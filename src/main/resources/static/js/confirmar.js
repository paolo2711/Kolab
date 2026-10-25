// un formulario con data-confirmar pregunta antes de enviarse; la csp no deja usar onsubmit

(function () {
    document.querySelectorAll('form[data-confirmar]').forEach(function (formulario) {
        formulario.addEventListener('submit', function (evento) {
            if (!window.confirm(formulario.dataset.confirmar)) {
                evento.preventDefault();
            }
        });
    });
})();
