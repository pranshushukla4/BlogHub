package in.scalive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostRequestDTO {
	
	@NotBlank(message="Title is requerd")
	private String title;
	@NotBlank(message="Content is requerd")
	private String content;
	@NotNull(message="categoryId is requerd")
	private Long categoryId;
	
	private Long authorId;
}
