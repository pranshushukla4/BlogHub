package in.scalive.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

	@NotBlank(message="Name is Requrd")
	private String name;
	@NotBlank(message="Email is Requrd")
	@Email(message="Email Formate shoukd be valid")
	private String email;
	@Size(min=6,message="Password must be 6 charcter long")
	@NotBlank(message="Password is Requrd")
	private String password;
	@NotBlank(message="About is Requrd")
	private String about;
	
}
