package controllers.howtodo;

import controllers.CRUD;
import controllers.Security;
import play.data.binding.Binder;
import play.db.Model;
import play.exceptions.TemplateNotFoundException;
import play.mvc.Before;

@CRUD.For(models.howtodo.Parameter.class)
public class ParameterCRUD extends CRUD {
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
	
	public static void showIndexPage(String id) throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Model object = type.findById(id);
        notFoundIfNull(object);
        try {
            render("howtodo/ParameterCRUD/showIndexPage.html", type, object);
        } catch (TemplateNotFoundException e) {
            render("howtodo/ParameterCRUD/showIndexPage.html", type, object);
        }
    }

	public static void showHashKeysPage(String id) throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		Model object = type.findById(id);
		notFoundIfNull(object);
		try {
			render("howtodo/ParameterCRUD/showHashKeysPage.html", type, object);
		} catch (TemplateNotFoundException e) {
			render("howtodo/ParameterCRUD/showHashKeysPage.html", type, object);
		}
	}

	public static void showSocialMediaPage(String id) throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		Model object = type.findById(id);
		notFoundIfNull(object);
		try {
			render("howtodo/ParameterCRUD/showSocialMediaPage.html", type, object);
		} catch (TemplateNotFoundException e) {
			render("howtodo/ParameterCRUD/showSocialMediaPage.html", type, object);
		}
	}
	
	public static void save(String id, String from) throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Model object = type.findById(id);
        notFoundIfNull(object);
        Binder.bindBean(params.getRootParamNode(), "object", object);
        validation.valid(object);
        if (validation.hasErrors()) {
            renderArgs.put("error", play.i18n.Messages.get("crud.hasErrors"));
            try {
                render(request.controller.replace(".", "/") + "/show.html", type, object);
            } catch (TemplateNotFoundException e) {
                render("CRUD/show.html", type, object);
            }
        }
        object._save();
        flash.success(play.i18n.Messages.get("crud.saved", type.modelName));
        if (params.get("_save") != null) {
            redirect("AdminPub.index");
        }
        if (from.equals("showIndexPage")) {
        	redirect(request.controller + ".showIndexPage", object._key());
		} else if (from.equals("showSocialMediaPage")) {
			redirect(request.controller + ".showSocialMediaPage", object._key());
		} else if (from.equals("showHashKeysPage")) {
			redirect(request.controller + ".showHashKeysPage", object._key());
		}
    }
}
