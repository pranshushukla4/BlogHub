package in.scalive.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.scalive.dto.AuthorResponseDTO;
import in.scalive.dto.AuthorUpdateDTO;
import in.scalive.entity.Author;
import in.scalive.service.AuthorService;

@RestController
@RequestMapping("/api/users")
public class AuthorController {
	private AuthorService authServ;

	@Autowired
	public AuthorController(AuthorService authServ) {
		this.authServ = authServ;
	}
	
	
	@GetMapping("/{id}")
	public ResponseEntity<AuthorResponseDTO> getUserById(@PathVariable Long id ){
		Author author = authServ.getUserById(id);
		AuthorResponseDTO authorDTO= new AuthorResponseDTO(id,author.getName(),author.getEmail(),author.getRoll(),author.getAbout());
		return  ResponseEntity.ok(authorDTO);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody AuthorUpdateDTO authUpdate,@RequestAttribute ("currentUserId")Long currentUserId, @RequestAttribute ("currentUserRoll")String currentUserRoll){
		if(!id.equals(currentUserId) && !currentUserRoll.equals("ADMIN")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"error\":You can update only your profile\"}");
		}
		Author updateAuthor= authServ.updateUser(id, authUpdate);
		AuthorResponseDTO respDTO= new AuthorResponseDTO(updateAuthor.getId(),updateAuthor.getName(),updateAuthor.getEmail(),updateAuthor.getRoll(),updateAuthor.getAbout());
		return ResponseEntity.ok(respDTO);
	}
	
	
	@GetMapping
	public ResponseEntity<List<AuthorResponseDTO>> getAllUsers(){
		List<Author> authorList= authServ.getAllUsers();
		List<AuthorResponseDTO> respList=new ArrayList<>();
		for(Author author:authorList) {
			AuthorResponseDTO respDTO= new AuthorResponseDTO(author.getId(),author.getName(),author.getEmail(),author.getRoll(),author.getAbout());
			respList.add(respDTO);
		}
		return ResponseEntity.ok(respList);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<String>  deleteUser( @PathVariable Long id, @RequestAttribute ("currentUserId")Long currentUserId, @RequestAttribute ("currentUserRoll")String currentUserRoll ){
		if(!id.equals(currentUserId) && !currentUserRoll.equals("ADMIN")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"error\":You can Delete only your profile\"}");
		}
		authServ.deleteUser(id);
		return ResponseEntity.ok("User deleted Succesfully");
	}
	
	
}
