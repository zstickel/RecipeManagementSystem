package recipes;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotBlank
    String name;
    @NotBlank
    String category;
    LocalDateTime date = LocalDateTime.now();
    @NotBlank
    String description;
    @NotBlank
    String authorEmail;
    @NotEmpty
    @ElementCollection
    List<String> ingredients;
    @NotEmpty
    @ElementCollection
    List<String> directions;
}
