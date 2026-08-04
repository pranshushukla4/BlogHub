package in.scalive.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice

public class GlobleExceptionHandler {
	@ExceptionHandler(value=ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex){
		ErrorResponse errorResponse= new ErrorResponse(HttpStatus.NOT_FOUND.value(),ex.getMessage());
		return new ResponseEntity<ErrorResponse>(errorResponse,HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(value=ResourceAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex){
		ErrorResponse errorResponse= new ErrorResponse(HttpStatus.CONFLICT.value(),ex.getMessage());
		return new ResponseEntity<ErrorResponse>(errorResponse,HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(value=MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String,String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
		Map<String,String>errorMap= new HashMap<>();
		BindingResult br=ex.getBindingResult();
		List<FieldError> errorList=br.getFieldErrors();
		
		for(FieldError ferror:errorList) {
			errorMap.put(ferror.getField(), ferror.getDefaultMessage());
		}
		
		return new ResponseEntity<Map<String,String>>(errorMap,HttpStatus.BAD_REQUEST);
	}
	
	
	@ExceptionHandler(value=Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception ex){
		ErrorResponse errorResponse= new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),ex.getMessage());
		return new ResponseEntity<ErrorResponse>(errorResponse,HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	
}
