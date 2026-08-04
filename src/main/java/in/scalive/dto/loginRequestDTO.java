package in.scalive.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class loginRequestDTO {
	
	@NotBlank(message="Email is Requrd")
	@Email(message="Email shoukd be valid")
	private String email;
	
	@NotBlank(message="Password is Requrd")
	private String password;
	
}
