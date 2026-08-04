package in.scalive.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.scalive.dto.AuthResponseDTO;
import in.scalive.dto.RegisterRequestDTO;
import in.scalive.dto.loginRequestDTO;
import in.scalive.entity.Author;
import in.scalive.exception.ResourceAlreadyExistsException;
import in.scalive.exception.ResourceNotFoundException;
import in.scalive.repository.AuthorRepository;
import jakarta.servlet.http.HttpSession;

@Service
public class AuthService {
	private AuthorRepository authRepo;
	
	@Autowired
	public AuthService(AuthorRepository authRepo) {
		this.authRepo = authRepo;
	}
	
	public AuthResponseDTO register(RegisterRequestDTO req) {
		// Step 1: check if alredy exists
		if(authRepo.existsByEmail(req.getEmail())) {
			throw new ResourceAlreadyExistsException("Email already register");
		}
		// Step2 convert DTO into entity
		Author author = new Author();
		author.setName(req.getName());
		author.setPassword(req.getPassword());
		author.setEmail(req.getEmail());
		author.setPassword(req.getPassword());
		author.setAbout(req.getAbout());
		author.setRoll("USER");
		
		// step 3 Save entity
		Author savedAuthor= (Author) authRepo.save(author);
		
		return (new AuthResponseDTO(savedAuthor.getId(), savedAuthor.getName(),savedAuthor.getEmail(),savedAuthor.getRoll(), "Registraction Succesfull"));
	}
	
	
	public AuthResponseDTO login(loginRequestDTO req,HttpSession session) {
		// Step 1: check if already exists
		Author author=authRepo.findByEmail(req.getEmail()).orElse(null);
		if(author==null) {
			throw new ResourceAlreadyExistsException("Invalid userId or Password");
		}
		if(!author.getPassword().equals(req.getPassword())) {
			throw new ResourceNotFoundException("Invalid userId or Password");	
		}
		
		session.setAttribute("userId", author.getId());
		session.setAttribute("userRole", author.getRoll());
		session.setAttribute("userName", author.getName());
		session.setAttribute("userEmail", author.getEmail());

		
		return (new AuthResponseDTO(
				author.getId(), 
				author.getName(),
				author.getEmail(),
				author.getRoll(), 
				"login Succesfull"));
	}
	
	
	public void logout(HttpSession session) {
		session.invalidate();
	}
	public AuthResponseDTO getCurrentUser(HttpSession session) {
		Long userId=(Long) session.getAttribute("userId");
		
		if(userId==null) {
			throw new ResourceNotFoundException("No user Logged In");
		}
		String userName=(String) session.getAttribute("userName");
		String userEmail=(String) session.getAttribute("userEmail");
		String userRoll=(String) session.getAttribute("userRoll");
		
		return new AuthResponseDTO(userId,userName,userEmail,userRoll,"Current user Data");	
	}
	
	
	
	
}
