document.addEventListener('submit', function (evento) {
    var formulario = evento.target;
    var mensaje = formulario.getAttribute('data-confirmar');
    if (mensaje && !window.confirm(mensaje)) {
        evento.preventDefault();
    }
});

document.addEventListener('click', function (evento) {
    var boton = evento.target.closest('[data-imprimir]');
    if (boton) {
        window.print();
    }
});

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('select[data-precio-objetivo]').forEach(function (selector) {
        selector.addEventListener('change', function () {
            var opcion = selector.options[selector.selectedIndex];
            var campo = document.getElementById(selector.getAttribute('data-precio-objetivo'));
            if (campo && opcion && opcion.getAttribute('data-precio')) {
                campo.value = opcion.getAttribute('data-precio');
            }
        });
    });

    var radios = document.querySelectorAll('input[name="accion"]');
    var bloquePago = document.getElementById('bloque-pago');
    if (radios.length > 0 && bloquePago) {
        var actualizar = function () {
            var vender = false;
            radios.forEach(function (radio) {
                if (radio.checked && radio.value === 'VENDER') {
                    vender = true;
                }
            });
            bloquePago.classList.toggle('oculto', !vender);
        };
        radios.forEach(function (radio) {
            radio.addEventListener('change', actualizar);
        });
        actualizar();
    }
});

document.addEventListener('DOMContentLoaded', function () {
    var meta = document.querySelector('meta[name="ruta-base"]');
    var base = meta ? meta.getAttribute('content') : '/';

    document.querySelectorAll('input[data-consulta-dni]').forEach(function (campo) {
        var tipo = campo.getAttribute('data-consulta-dni');
        var formulario = campo.form;
        var estado = campo.parentElement.querySelector('[data-estado-dni]');
        var nombres = formulario.querySelector('[name="nombres"]');
        var apellidos = formulario.querySelector('[name="apellidos"]');
        var inicial = campo.value;
        var ultimo = '';

        var mostrar = function (texto, clase) {
            if (!estado) {
                return;
            }
            estado.textContent = texto;
            estado.classList.remove('dni-ok', 'dni-aviso', 'dni-error');
            if (clase) {
                estado.classList.add(clase);
            }
        };

        campo.addEventListener('input', function () {
            campo.value = campo.value.replace(/\D/g, '').slice(0, 8);
            var dni = campo.value;
            if (dni.length !== 8 || dni === ultimo || (inicial && dni === inicial)) {
                return;
            }
            ultimo = dni;
            mostrar('Consultando DNI...', null);
            campo.classList.add('consultando');
            fetch(base + 'consultas/dni/' + tipo + '/' + dni, {headers: {'Accept': 'application/json'}})
                .then(function (respuesta) {
                    return respuesta.json();
                })
                .then(function (datos) {
                    if (datos.encontrado && !datos.registrado) {
                        if (nombres) {
                            nombres.value = datos.nombres;
                        }
                        if (apellidos) {
                            apellidos.value = datos.apellidos;
                        }
                        mostrar(datos.mensaje, 'dni-ok');
                    } else if (datos.registrado) {
                        mostrar(datos.mensaje + ': ' + datos.nombres + ' ' + datos.apellidos, 'dni-aviso');
                    } else {
                        mostrar(datos.mensaje, 'dni-error');
                    }
                })
                .catch(function () {
                    mostrar('No se pudo consultar el DNI. Ingrese los datos manualmente', 'dni-error');
                })
                .finally(function () {
                    campo.classList.remove('consultando');
                });
        });
    });

    var selectorRol = document.querySelector('select[data-rol-usuario]');
    var bloqueConductor = document.querySelector('[data-solo-conductor]');
    if (selectorRol && bloqueConductor) {
        var alternar = function () {
            var esConductor = selectorRol.value === 'CONDUCTOR';
            bloqueConductor.classList.toggle('oculto', !esConductor);
            var lista = bloqueConductor.querySelector('select');
            if (lista) {
                lista.required = esConductor;
            }
        };
        selectorRol.addEventListener('change', alternar);
        alternar();
    }

    var insignia = document.querySelector('[data-conteo-alertas]');
    if (insignia) {
        var anterior = parseInt(insignia.textContent, 10) || 0;
        var revisar = function () {
            fetch(base + 'alertas/conteo', {headers: {'Accept': 'application/json'}})
                .then(function (respuesta) {
                    return respuesta.ok ? respuesta.json() : null;
                })
                .then(function (datos) {
                    if (!datos) {
                        return;
                    }
                    var total = datos.pendientes;
                    insignia.textContent = total;
                    insignia.classList.toggle('oculto', total === 0);
                    var aviso = document.querySelector('[data-aviso-alertas]');
                    if (total > 0) {
                        if (!aviso) {
                            aviso = document.createElement('a');
                            aviso.className = 'alerta alerta-critica';
                            aviso.setAttribute('data-aviso-alertas', '');
                            aviso.setAttribute('role', 'alert');
                            aviso.href = base + 'alertas';
                            var contenido = document.querySelector('.contenido');
                            var cabecera = contenido ? contenido.querySelector('.barra-superior') : null;
                            if (cabecera) {
                                cabecera.insertAdjacentElement('afterend', aviso);
                            }
                        }
                        aviso.textContent = total === 1
                            ? 'Hay 1 alerta de viaje sin atender. Haga clic para revisarla.'
                            : 'Hay ' + total + ' alertas de viaje sin atender. Haga clic para revisarlas.';
                        if (total > anterior) {
                            aviso.classList.remove('parpadeo');
                            void aviso.offsetWidth;
                            aviso.classList.add('parpadeo');
                        }
                    } else if (aviso) {
                        aviso.remove();
                    }
                    anterior = total;
                })
                .catch(function () {
                });
        };
        setInterval(revisar, 30000);
    }
});
