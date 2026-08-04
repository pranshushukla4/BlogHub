package in.scalive.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.scalive.dto.AuthorUpdateDTO;
import in.scalive.entity.Author;
import in.scalive.exception.ResourceNotFoundException;
import in.scalive.repository.AuthorRepository;

@Service
public class AuthorService {
	private AuthorRepository authRepo;
	
	@Autowired
	public AuthorService(AuthorRepository authRepo) {
			this.authRepo = authRepo;
	}
	
	public List<Author> getAllUsers(){
		return authRepo.findAll();
	}
	
	public Author getUserById(Long id) {
		Author author=authRepo.findById(id).orElse(null);
		if(author==null) {
			throw new ResourceNotFoundException("No Author with id "+id+" found");
		}
		return author;
	}
	
	public Author updateUser(Long id, AuthorUpdateDTO updAuthor) {
		Author author= getUserById(id);
		if(updAuthor==null && updAuthor.getEmail()==null && updAuthor.getAbout()==null)
			throw new RuntimeException("Empty Object Not Allowed");
		if(updAuthor.getName()!=null && updAuthor.getName().isBlank())
				throw new RuntimeException("Name can not be blank");
		if(updAuthor.getAbout()!=null && updAuthor.getAbout().isBlank())
			throw new RuntimeException("About can not be blank");
		
		if(updAuthor.getName()!=null )
			author.setName(updAuthor.getName());
		if(updAuthor.getAbout()!=null )
			author.setAbout(updAuthor.getAbout());
		if(updAuthor.getEmail()!=null )
			author.setEmail(updAuthor.getEmail());
	
		return authRepo.save(author);
	}
	
	public void deleteUser(Long id) {
		Author author= getUserById(id);
		authRepo.delete(author);
		
		
		
	}
}
