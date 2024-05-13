package recipes;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeService {
    public static RecipeDTO recipeToDTO(Recipe recipe) {
        return new RecipeDTO( recipe.getName(), recipe.getCategory(), recipe.getDate(), recipe.getDescription(),
                recipe.getIngredients(), recipe.getDirections());
    }

    public static List<RecipeDTO> recipesToDTO(List<Recipe> recipes) {
        return recipes.stream().map(RecipeService::recipeToDTO).toList();
    }
}
