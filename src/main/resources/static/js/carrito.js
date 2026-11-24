(function () {
    'use strict';

    var CLAVE = 'cayo_carrito';

    function leer() {
        try {
            var datos = JSON.parse(localStorage.getItem(CLAVE) || '[]');
            if (!Array.isArray(datos)) return [];
            
            return datos.filter(function (it) {
                return it && Number.isSafeInteger(Number(it.id)) && Number(it.id) > 0 &&
                    typeof it.nombre === 'string' &&
                    typeof it.categoria === 'string' && typeof it.isla === 'string' &&
                    typeof it.precio === 'number' && Number.isFinite(it.precio) && it.precio >= 0 &&
                    Number.isSafeInteger(it.cantidad) && it.cantidad > 0 &&
                    Number.isFinite(it.precio * it.cantidad);
            });
        } catch (e) {
            return [];
        }
    }

    function guardar(lista) {
        localStorage.setItem(CLAVE, JSON.stringify(lista));
    }

    function dinero(n) {
        var num = Number(n);
        if (isNaN(num)) return '';
        return '$ ' + num.toLocaleString('es-CO', { maximumFractionDigits: 0 });
    }

    function totalCarrito(lista) {
        return lista.reduce(function (acc, it) {
            return acc + (Number(it.precio) || 0) * (Number(it.cantidad) || 1);
        }, 0);
    }

    function actualizarBadge() {
        var contador = document.getElementById('carrito-contador');
        if (!contador) return;
        var lista = leer();
        var cant = lista.reduce(function (acc, it) { return acc + (Number(it.cantidad) || 1); }, 0);
        contador.textContent = cant;
        contador.style.display = cant > 0 ? 'inline-flex' : 'none';
    }

    function renderPopup() {
        var contenedor = document.getElementById('carrito-items');
        var totalEl = document.getElementById('carrito-total');
        var pagarBtn = document.getElementById('carrito-pagar');
        var ocultos = document.getElementById('carrito-items-hidden');
        if (!contenedor) return;

        var lista = leer();
        contenedor.innerHTML = '';
        if (ocultos) ocultos.innerHTML = '';

        if (lista.length === 0) {
            var vacio = document.createElement('p');
            vacio.className = 'carrito-vacio';
            vacio.textContent = 'Aún no has agregado servicios.';
            contenedor.appendChild(vacio);
        } else {
            lista.forEach(function (it, idx) {
                var item = document.createElement('div');
                item.className = 'carrito-item';
                item.innerHTML =
                    '<div class="carrito-item-info">' +
                        '<strong>' + escapeHtml(it.nombre) + '</strong>' +
                        '<small>' + escapeHtml(it.categoria) + ' · ' + escapeHtml(it.isla) + '</small>' +
                    '</div>' +
                    '<div class="carrito-item-precio">' +
                        '<span>' + dinero(it.precio) + ' × ' + it.cantidad + '</span>' +
                        '<strong>' + dinero((Number(it.precio) || 0) * (Number(it.cantidad) || 1)) + '</strong>' +
                    '</div>' +
                    '<button type="button" class="carrito-item-quitar" data-idx="' + idx + '" aria-label="Quitar">&times;</button>';
                contenedor.appendChild(item);

                if (ocultos) {
                    var idCampo = document.createElement('input');
                    idCampo.type = 'hidden';
                    idCampo.name = 'idOferta';
                    idCampo.value = it.id;
                    ocultos.appendChild(idCampo);

                    var cantidadCampo = document.createElement('input');
                    cantidadCampo.type = 'hidden';
                    cantidadCampo.name = 'cantidad';
                    cantidadCampo.value = it.cantidad;
                    ocultos.appendChild(cantidadCampo);
                }
            });
        }

        if (pagarBtn) {
            var vacio = lista.length === 0;
            pagarBtn.disabled = vacio;
            pagarBtn.classList.toggle('disabled', vacio);
            pagarBtn.setAttribute('aria-disabled', String(vacio));
            if (vacio) {
                pagarBtn.setAttribute('tabindex', '-1');
            } else {
                pagarBtn.removeAttribute('tabindex');
            }
        }

        if (totalEl) {
            totalEl.textContent = dinero(totalCarrito(lista));
        }

        
        contenedor.querySelectorAll('.carrito-item-quitar').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var idx = Number(this.getAttribute('data-idx'));
                var l = leer();
                l.splice(idx, 1);
                guardar(l);
                renderPopup();
                actualizarBadge();
            });
        });
    }

    function escapeHtml(str) {
        return String(str == null ? '' : str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function agregarItem(data) {
        if (!Number.isSafeInteger(Number(data.id)) || Number(data.id) <= 0) return;
        var lista = leer();
        var existente = lista.find(function (it) { return String(it.id) === String(data.id); });
        if (existente) {
            existente.cantidad = (Number(existente.cantidad) || 1) + 1;
        } else {
            lista.push({
                id: data.id,
                nombre: data.nombre,
                categoria: data.categoria,
                isla: data.isla,
                precio: Number(data.precio) || 0,
                cantidad: 1
            });
        }
        guardar(lista);
        actualizarBadge();
        renderPopup();
        abrirPanel();
    }

    function abrirPanel() {
        renderPopup();
        var panel = document.getElementById('carrito-panel');
        var overlay = document.getElementById('carrito-overlay');
        if (panel) panel.classList.add('abierto');
        if (overlay) overlay.classList.add('visible');
        var boton = document.getElementById('carrito-boton');
        if (boton) boton.setAttribute('aria-expanded', 'true');
        var cerrar = document.getElementById('carrito-cerrar');
        if (cerrar) cerrar.focus();
    }

    function cerrarPanel() {
        var panel = document.getElementById('carrito-panel');
        var overlay = document.getElementById('carrito-overlay');
        if (panel) panel.classList.remove('abierto');
        if (overlay) overlay.classList.remove('visible');
        var boton = document.getElementById('carrito-boton');
        if (boton) {
            boton.setAttribute('aria-expanded', 'false');
            boton.focus();
        }
    }

    function vaciar() {
        guardar([]);
        actualizarBadge();
        renderPopup();
    }

    
    window.CayoCarrito = {
        leer: leer,
        guardar: guardar,
        total: totalCarrito,
        dinero: dinero,
        vaciar: vaciar
    };

    document.addEventListener('DOMContentLoaded', function () {
        actualizarBadge();
        renderPopup();

        
        document.querySelectorAll('.agregar-carrito').forEach(function (btn) {
            btn.addEventListener('click', function () {
                agregarItem({
                    id: this.getAttribute('data-id'),
                    nombre: this.getAttribute('data-nombre'),
                    categoria: this.getAttribute('data-categoria'),
                    isla: this.getAttribute('data-isla'),
                    precio: this.getAttribute('data-precio')
                });
            });
        });

        
        var boton = document.getElementById('carrito-boton');
        if (boton) boton.addEventListener('click', abrirPanel);

        var cerrar = document.getElementById('carrito-cerrar');
        if (cerrar) cerrar.addEventListener('click', cerrarPanel);

        var overlay = document.getElementById('carrito-overlay');
        if (overlay) overlay.addEventListener('click', cerrarPanel);

        var vaciarBtn = document.getElementById('carrito-vaciar');
        if (vaciarBtn) vaciarBtn.addEventListener('click', vaciar);

        var pagarForm = document.getElementById('carrito-pagar-form');
        if (pagarForm) pagarForm.addEventListener('submit', function (event) {
            if (leer().length === 0) {
                event.preventDefault();
                renderPopup();
            }
        });

        document.addEventListener('keydown', function (event) {
            var panel = document.getElementById('carrito-panel');
            if (event.key === 'Escape' && panel && panel.classList.contains('abierto')) cerrarPanel();
        });
    });

    window.addEventListener('pageshow', function () {
        actualizarBadge();
        renderPopup();
    });

    window.addEventListener('storage', function (event) {
        if (event.key === CLAVE || event.key === null) {
            actualizarBadge();
            renderPopup();
        }
    });
})();
