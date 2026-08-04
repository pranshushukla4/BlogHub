package in.scalive.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorUpdateDTO {
	@Size(min=1,message="Ename is requrd")
	private String name;
	@Email(message="Email should be valid")
	private String email;
	@Size(min=1,message="About is requrd")
	private String about;
	
	
}
