package recipes;

import jakarta.persistence.EntityExistsException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class RecipeController {

    @Autowired private RecipeRepository recipeRepository;

    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AppUserRepository repository;
    @PostMapping("/api/recipe/new")
    public ResponseEntity<?> postRecipe(@Valid @RequestBody RecipeDTI recipeReceived, BindingResult result) {
        if(result.hasErrors()){
            return ResponseEntity.badRequest().build();
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Recipe recipe = new Recipe();
        recipe.name = recipeReceived.getName();
        recipe.category = recipeReceived.getCategory();
        recipe.description = recipeReceived.getDescription();
        recipe.directions = recipeReceived.getDirections();
        recipe.authorEmail = username;
        recipe.ingredients = recipeReceived.getIngredients();
        System.out.println("Recipe uploader's email: " + authentication.getName());
        recipeRepository.save(recipe);
        return ResponseEntity.ok(Map.of("id", recipe.getId()));
    }

    @PostMapping("/actuator/shutdown")
    public ResponseEntity<?> postShutdown(){
        return ResponseEntity.ok(Map.of("status","shutdown"));
    }

    @PostMapping("/api/register")
    public ResponseEntity<?> postRegister(@Valid @RequestBody RegistrationInfo registrationInfo, BindingResult result){
        if(result.hasErrors()){
            return ResponseEntity.badRequest().build();
        }
        try {
            System.out.println("In the try");
            AppUser user = new AppUser();
            user.setUsername(registrationInfo.getEmail());
            user.setPassword(passwordEncoder.encode(registrationInfo.getPassword()));
            user.setAuthority("USER");
            if (repository.findAppUserByUsername(registrationInfo.getEmail()).isEmpty()) {
                repository.save(user);
                return ResponseEntity.ok(Map.of("RegistrationStatus", "Success"));
            }else{
                return ResponseEntity.badRequest().build();
            }
        }catch(EntityExistsException entityExistsException){
            System.out.println("Made it to the catch");
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/api/recipe/{id}")
    public ResponseEntity<?> putRecipe(@PathVariable long id, @Valid @RequestBody RecipeDTI recipeReceived, BindingResult result) {
        if(result.hasErrors()){
            return ResponseEntity.badRequest().build();
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<Recipe> recipe = recipeRepository.findById(id);
        if(recipe.isPresent()) {
            if(recipe.get().getAuthorEmail().equals(authentication.getName())) {
                recipe.get().setName(recipeReceived.getName());
                recipe.get().setDescription(recipeReceived.getDescription());
                recipe.get().setCategory(recipeReceived.getCategory());
                recipe.get().setDate(LocalDateTime.now());
                recipe.get().setIngredients(recipeReceived.getIngredients());
                recipe.get().setDirections(recipeReceived.getDirections());
                recipeRepository.save(recipe.get());
                return ResponseEntity.noContent().build();
            }else{
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
            }
        }else{
            return ResponseEntity.notFound().build();
        }

    }
    @GetMapping("/api/recipe/{id}")
    public ResponseEntity<?> getRecipeById(@PathVariable long id) {
        Optional<Recipe> recipe = recipeRepository.findById(id);

        if (recipe.isPresent()) {
            return ResponseEntity.ok(RecipeService.recipeToDTO(recipe.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/api/recipe/search/")
    public ResponseEntity<?> searchRecipe(@RequestParam(required = false) String category, @RequestParam(required = false) String name) {
        if((category == null && name == null) || (category != null && name != null)){
            return ResponseEntity.badRequest().build();
        }
        List<Recipe> recipes = new ArrayList<>();
        if (category != null) {
            recipes = recipeRepository.findByCategoryIgnoreCaseOrderByDateDesc(category);
        } else {
            recipes = recipeRepository.findByNameContainingIgnoreCaseOrderByDateDesc(name);
        }
        return ResponseEntity.ok(RecipeService.recipesToDTO(recipes));
    }

    @DeleteMapping("/api/recipe/{id}")
    public ResponseEntity<?> deleteRecipeById(@PathVariable long id) {
        Optional<Recipe> recipe = recipeRepository.findById(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (recipe.isPresent()) {
            if (recipe.get().getAuthorEmail().equals(authentication.getName())){
                recipeRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            }else{
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
            }
        }
        return ResponseEntity.notFound().build();
    }
}
