package controllers.howtodo;

import java.io.File;

import controllers.howtodo.AdminPub;
import controllers.howtodo.ApplicationPub;
import controllers.CRUD;
import controllers.Security;
import controllers.CRUD.For;
import models.howtodo.Article;
import play.mvc.Before;
import play.vfs.VirtualFile;

@CRUD.For(models.howtodo.TheSystem.class)
public class TheSystemCRUD extends CRUD {
	
	@Before
	static void globals() {
		if (AdminPub.getLoggedUserInstitution() == null || AdminPub.getLoggedUserInstitution().getUser() == null) {
			ApplicationPub.index();
		} 
		renderArgs.put("poweradmin", "lucascorreiaevangelista@gmail.com".equals(AdminPub.getLoggedUserInstitution().getUser().getEmail()) ? "true" : "false");
		renderArgs.put("logged", AdminPub.getLoggedUserInstitution().getUser().id);
		renderArgs.put("enableUser", Security.enableMenu() ? "true" : "false");
		renderArgs.put("idu", AdminPub.getLoggedUserInstitution().getUser().getId());
		renderArgs.put("id", AdminPub.getLoggedUserInstitution().getInstitution() != null ? AdminPub.getLoggedUserInstitution().getInstitution().getId() : null);
		renderArgs.put("institutionName", AdminPub.getLoggedUserInstitution().getInstitution() != null ? AdminPub.getLoggedUserInstitution().getInstitution().getInstitution() : null);
	}
	
}
