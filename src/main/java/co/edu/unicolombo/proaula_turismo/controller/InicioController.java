package co.edu.unicolombo.proaula_turismo.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unicolombo.proaula_turismo.model.Oferta;
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

    @GetMapping("/catalogo")
    public String catalogo(Model modelo) {
        // Datos de ejemplo mientras la base de datos no está conectada.
        // Cuando se conecte MySQL, esta lista se reemplaza por el repositorio de ofertas.
        modelo.addAttribute("ofertas", List.of(
                crearOferta("Día de playa en Barú",
                        "Lancha ida y vuelta, almuerzo típico y tiempo libre en Playa Blanca.",
                        "Playa", "Barú", "185000", 24, 2),
                crearOferta("Snorkel en las Islas del Rosario",
                        "Recorrido guiado por los arrecifes con equipo de snorkel incluido.",
                        "Snorkel", "Islas del Rosario", "230000", 18, 3),
                crearOferta("Atardecer en Tierra Bomba",
                        "Paseo en lancha, música y cóctel de bienvenida frente al mar.",
                        "Experiencia", "Tierra Bomba", "150000", 30, 4),
                crearOferta("Tour de día completo por el Rosario",
                        "Visita a las islas, paradas para nadar y almuerzo caribeño.",
                        "Tour", "Islas del Rosario", "320000", 16, 2),
                crearOferta("Paseo en lancha por la bahía",
                        "Recorrido por la bahía de Cartagena con guía local a bordo.",
                        "Transporte", "Cartagena", "95000", 40, 5),
                crearOferta("Playa tranquila en Barú",
                        "Acceso a playa privada, sombrilla y bebida de bienvenida.",
                        "Playa", "Barú", "120000", 20, 6)));
        return "catalogo";
    }

    private Oferta crearOferta(String nombre, String descripcion, String categoria, String isla,
            String precio, int cuposMaximos, int idProveedor) {
        Oferta oferta = new Oferta();
        oferta.setNombreOferta(nombre);
        oferta.setDescripcion(descripcion);
        oferta.setCategoria(categoria);
        oferta.setIsla(isla);
        oferta.setPrecio(new BigDecimal(precio));
        oferta.setCuposMaximos(cuposMaximos);
        oferta.setEstadoOferta("Publicada");
        oferta.setIdProveedor(idProveedor);
        return oferta;
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
        return "redirect:/catalogo"; 
    }
}