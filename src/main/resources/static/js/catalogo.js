(() => {
    const carrusel = document.getElementById('experiencias-carrusel');
    if (!carrusel) return;

    const seccion = carrusel.closest('.catalogo-experiencias');
    const anterior = seccion.querySelector('[data-direccion="-1"]');
    const siguiente = seccion.querySelector('[data-direccion="1"]');
    let animacion = null;
    let destino = carrusel.scrollLeft;

    function detenerAnimacion() {
        cancelAnimationFrame(animacion);
        animacion = null;
        destino = carrusel.scrollLeft;
    }

    function desplazar(direccion) {
        const tarjeta = carrusel.querySelector('.catalogo-experiencia');
        const espacio = parseFloat(getComputedStyle(carrusel).columnGap) || 0;
        const paso = tarjeta.getBoundingClientRect().width + espacio;
        const limite = carrusel.scrollWidth - carrusel.clientWidth;
        const origen = carrusel.scrollLeft;


        destino = Math.max(0, Math.min(limite,
            (animacion === null ? origen : destino) + direccion * paso));
        cancelAnimationFrame(animacion);
        animacion = null;

        if (Math.abs(destino - origen) < 1) {
            carrusel.scrollLeft = destino;
            actualizarFlechas();
            return;
        }

        const distancia = destino - origen;
        const duracion = 750;
        let inicio = null;

        function animar(tiempo) {
            if (inicio === null) inicio = tiempo;
            const progreso = Math.min((tiempo - inicio) / duracion, 1);
            const suavizado = (1 - Math.cos(Math.PI * progreso)) / 2;
            carrusel.scrollLeft = origen + distancia * suavizado;

            if (progreso < 1) {
                animacion = requestAnimationFrame(animar);
            } else {
                animacion = null;
                actualizarFlechas();
            }
        }

        animacion = requestAnimationFrame(animar);
    }

    function actualizarFlechas() {
        const limite = carrusel.scrollWidth - carrusel.clientWidth;
        anterior.disabled = carrusel.scrollLeft <= 2;
        siguiente.disabled = carrusel.scrollLeft >= limite - 2;
    }

    seccion.querySelectorAll('.catalogo-experiencias-flecha').forEach((boton) => {
        boton.addEventListener('click', () => {
            desplazar(Number(boton.dataset.direccion));
        });
    });

    carrusel.addEventListener('scroll', actualizarFlechas, { passive: true });
    carrusel.addEventListener('pointerdown', detenerAnimacion, { passive: true });
    carrusel.addEventListener('touchstart', detenerAnimacion, { passive: true });
    carrusel.addEventListener('wheel', detenerAnimacion, { passive: true });
    carrusel.addEventListener('keydown', detenerAnimacion);
    new ResizeObserver(() => {
        detenerAnimacion();
        actualizarFlechas();
    }).observe(carrusel);
    actualizarFlechas();
})();
