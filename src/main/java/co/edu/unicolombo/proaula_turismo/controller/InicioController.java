package co.edu.unicolombo.proaula_turismo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unicolombo.proaula_turismo.model.Turista;
import co.edu.unicolombo.proaula_turismo.model.Usuario;

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

    @GetMapping("/registro-agencia")
    public String registroAgencia() {
        return "registro-agencia";
    }

    @GetMapping("/registro")
    public String registroPorTipo(@RequestParam(name = "tipo", required = false) String tipo) {
        if ("proveedor".equalsIgnoreCase(tipo)) {
            return "registro-proveedor";
        }
        if ("agencia".equalsIgnoreCase(tipo)) {
            return "registro-agencia";
        }
        return "registro-turista";
    }

    // --- PRUEBA ---
    @PostMapping("/registro")
    public String procesarRegistro(Usuario nuevoUsuario, Turista turista,
            @RequestParam(name = "rnt", required = false) MultipartFile rnt) {
        System.out.println("=========================================");
        System.out.println("¡NUEVO REGISTRO RECIBIDO!");
        System.out.println("Nombre: " + nuevoUsuario.getNombres());
        System.out.println("Documento: " + nuevoUsuario.getDocumento());
        System.out.println("Correo: " + nuevoUsuario.getCorreo());
        System.out.println("Teléfono: " + nuevoUsuario.getTelefono());
        System.out.println("Tipo de usuario: " + nuevoUsuario.getTipoUsuario());
        if (turista.getTipoTurista() != null && !turista.getTipoTurista().isBlank()) {
            System.out.println("Tipo de turista: " + turista.getTipoTurista());
            System.out.println("Nacionalidad: " + turista.getNacionalidad());
        }
        if (rnt != null && !rnt.isEmpty()) {
            System.out.println("RNT: " + rnt.getOriginalFilename() + " (" + rnt.getSize() + " bytes)");
        }
        System.out.println("=========================================");
        
        return "redirect:/login"; 
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String correo, @RequestParam String contrasena) {
        System.out.println("Intento de login - Correo: " + correo);
        return "redirect:/"; 
    }
}