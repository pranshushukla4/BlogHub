package in.scalive.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import in.scalive.dto.PostRequestDTO;
import in.scalive.dto.PostResponseDTO;
import in.scalive.dto.PostUpdateDTO;
import in.scalive.entity.Author;
import in.scalive.entity.Category;
import in.scalive.entity.Post;
import in.scalive.exception.ResourceNotFoundException;
import in.scalive.repository.AuthorRepository;
import in.scalive.repository.CategoryRepository;
import in.scalive.repository.PostRepository;

@Service
public class PostService {
	private PostRepository pRepo;
	private CategoryRepository cRepo;
	private AuthorRepository aRepo;
	
	@Autowired
	public PostService(PostRepository pRepo, CategoryRepository cRepo, AuthorRepository aRepo) {
		this.pRepo = pRepo;
		this.cRepo = cRepo;
		this.aRepo = aRepo;
	}
	
	public Post createPost(PostRequestDTO prDTO) {
		if(prDTO.getAuthorId()==null) {
			throw new RuntimeException("Author Id is Complesary!");
		}
		
		Author author=aRepo.findById(prDTO.getAuthorId()).orElse(null);
		if(author==null) {
			throw new ResourceNotFoundException("Author not found with id: "+prDTO.getAuthorId());
		}
		
		Category cat= cRepo.findById(prDTO.getCategoryId()).orElse(null);
		if(cat==null) {
			throw new ResourceNotFoundException("Category not found with id: "+prDTO.getCategoryId());
		}
		
		Post post=new Post();
		post.setTitle(prDTO.getTitle());
		post.setContent(prDTO.getContent());
		post.setCreatedAt(LocalDateTime.now());
		post.setAuthor(author);
		post.setCategory(cat);
		
		return pRepo.save(post);
	}
	
	
	public List<Post> getAllPost(){
		return pRepo.findAll();
	}
	
	public Page<PostResponseDTO> getAllPosts(int page, int size, String sortBy,String sortDir){
		Sort sort=sortDir.equalsIgnoreCase("DESC")? Sort.by(sortBy).descending():Sort.by(sortBy).ascending();
		Pageable pageable=PageRequest.of(page, size,sort);
		Page<Post>postsPage =pRepo.findAll(pageable);
		
		List<PostResponseDTO> dtoList= new ArrayList<>();
		for(Post post:postsPage.getContent()) {
			PostResponseDTO dto=new PostResponseDTO();
			dto.setId(post.getId());
			dto.setTitle(post.getTitle());
			dto.setCategoryId(post.getCategory().getId());
			dto.setCategoryName(post.getCategory().getCatName());
			dto.setAuthorId(post.getAuthor().getId());
			dto.setAuthorName(post.getAuthor().getName());
			dto.setCreateDateTime(post.getCreatedAt());
			dto.setContent(post.getContent());
			dtoList.add(dto);
		}
		Page<PostResponseDTO>pageList =new PageImpl<>(dtoList,pageable,postsPage.getTotalElements());
		return pageList;
	}
	
	public Post getPostById(Long postId) {
		Post post=pRepo.findById(postId).orElse(null);
		if(post==null) {
			throw new ResourceNotFoundException("Post not found with id: "+postId);
		}
		return post;
	}
	
	public List<Post> searchPosts(String term){
		 return pRepo.findByTitleContainingOrContentContaining(term, term.toLowerCase());
	}
	
	public List<Post> getPostsByAuthor(Long authorId){
		Author author=aRepo.findById(authorId).orElse(null);
		if(author==null) {
			throw new ResourceNotFoundException("Author not found with id: "+authorId);
		}
		return pRepo.findByAuthor(author);
		
	}
	
	public Post updatePost(Long postId,PostUpdateDTO postUpd) {
		Post post= getPostById(postId);
		if(postUpd==null || postUpd.getTitle()==null && postUpd.getAuthorId()==null && postUpd.getCategoryId()==null && postUpd.getContent()==null) {
			throw new RuntimeException("At least one fiels must be Present for updatetion");
		}
		if(postUpd.getTitle()!=null) {
			post.setTitle(postUpd.getTitle());
		}
		if(postUpd.getContent()!=null) {
			post.setContent(postUpd.getContent());
		}
		if(postUpd.getAuthorId()!=null) {
			Author author=aRepo.findById(postUpd.getAuthorId()).orElse(null);
			if(author==null) {
				throw new ResourceNotFoundException("Author not found with id: "+postUpd.getAuthorId());
			}
			post.setAuthor(author);
		}
		
		if(postUpd.getCategoryId()!=null) {
			Category cat= cRepo.findById(postUpd.getCategoryId()).orElse(null);
			if(cat==null) {
				throw new ResourceNotFoundException("Category not found with id: "+postUpd.getCategoryId());
			}
			post.setCategory(cat);
		}
		
		return pRepo.save(post);
	}
	
	
	// delete post...
	
	public void deletePost(Long postId) {
		Post post= getPostById(postId);
		pRepo.delete(post);		
	}
	
	
	
	
	
	
}
