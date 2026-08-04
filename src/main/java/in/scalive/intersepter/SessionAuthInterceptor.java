package in.scalive.intersepter;

import java.io.PrintWriter;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class SessionAuthInterceptor implements HandlerInterceptor{

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// TODO Auto-generated method stub
		
		HttpSession session=request.getSession(false);
		System.out.println("Path "+request.getRequestURI());
		System.out.println("Method "+request.getMethod());
		System.out.println("Session present ? :"+(session!=null));
		
		if(session !=null) {
			System.out.println("session id:"+session.getId());
			System.out.println("Userid:"+session.getAttribute("userId"));
		}
		
		if(session==null|| session.getAttribute("userId")==null) {
			
			response.setStatus(401);
			response.setContentType("application/json");
			PrintWriter pWriter=response.getWriter();
			pWriter.write("{\"error\" : \"Plese login first\"}");
			return false;
		}
		Long userId=(Long) session.getAttribute("userId");
		String userRoll=(String) session.getAttribute("userRoll");
		
		request.setAttribute("currentUserId", userId);
		request.setAttribute("currentUserRoll", userRoll);

		String path=request.getRequestURI();
		String method=request.getMethod();
		
		if(path.startsWith("/api/categories")) {
			if(!method.equals("GET") && !"ADMIN".equals(userRoll)) {
				response.setStatus(403);
				response.setContentType("application/json");
				PrintWriter pWriter=response.getWriter();
				pWriter.write("{\"error\" : \"Plese accese requeard\"}");
				return false;
			}
		}
		return true;
	}
	
	
}
