package in.scalive.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryRequestDTO {
	@NotBlank(message="Category name is requierd")
	private String catName;
	@NotBlank(message="Category description is requierd")
	private String descr; 
}

