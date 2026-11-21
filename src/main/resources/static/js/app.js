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
