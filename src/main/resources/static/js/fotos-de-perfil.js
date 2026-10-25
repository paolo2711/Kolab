// vista previa de editar perfil: la foto o la portada elegida se ve al toque, con una direccion blob:

(function () {
    const formulario = document.getElementById('formPerfil');
    if (!formulario) {
        return;
    }

    // una foto: el campo, la imagen de la vista previa, lo que sale sin foto y la casilla de quitar
    function previa(campo, imagen, sinFoto, quitar) {
        const actual = imagen.getAttribute('src');
        let elegida = null;

        function pintar() {
            const foto = elegida || (quitar && quitar.checked ? null : actual);
            imagen.hidden = !foto;
            sinFoto.hidden = !!foto;
            if (foto) {
                imagen.src = foto;
            }
        }

        campo.addEventListener('change', function () {
            if (elegida) {
                URL.revokeObjectURL(elegida);
            }
            const archivo = campo.files[0];
            elegida = archivo && /^image\/(jpeg|png)$/.test(archivo.type) ? URL.createObjectURL(archivo) : null;
            pintar();
        });
        if (quitar) {
            quitar.addEventListener('change', pintar);
        }
    }

    previa(formulario.querySelector('#foto'), document.getElementById('vpFoto'),
        document.getElementById('vpIniciales'), formulario.querySelector('#quitarFoto1'));
    previa(formulario.querySelector('#portada'), document.getElementById('vpPortada'),
        document.getElementById('vpFranja'), formulario.querySelector('#quitarPortada1'));
})();
