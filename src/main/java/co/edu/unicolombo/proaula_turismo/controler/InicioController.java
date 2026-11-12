package co.edu.unicolombo.proaula_turismo.controler;

import co.edu.unicolombo.proaula_turismo.model.Usuario;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller 
public class InicioController {

    @GetMapping ("/")
    public String index(){
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro-turista")
    public String registro() {
        return "registro-turista"; 
    }

    // --- PRUEBA ---
    @PostMapping("/registro")
    public String procesarRegistro(Usuario nuevoUsuario) {
        System.out.println("=========================================");
        System.out.println("¡NUEVO REGISTRO RECIBIDO!");
        System.out.println("Nombre: " + nuevoUsuario.getNombres());
        System.out.println("Documento: " + nuevoUsuario.getDocumento());
        System.out.println("Correo: " + nuevoUsuario.getCorreo());
        System.out.println("Teléfono: " + nuevoUsuario.getTelefono());
        System.out.println("Contraseña: " + nuevoUsuario.getContrasena());
        System.out.println("Tipo de usuario: " + nuevoUsuario.getTipoUsuario());
        System.out.println("=========================================");
        
        return "redirect:/login"; 
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String correo, @RequestParam String contrasena) {
        System.out.println("Intento de login - Correo: " + correo);
        return "redirect:/"; 
    }
}