// Único script do site: abre e fecha o menu no celular. O site funciona sem ele (ver <noscript> no layout).
(function () {
  var botao = document.querySelector('.menu-btn');
  var abas = document.getElementById('abas');
  if (!botao || !abas) return;
  botao.addEventListener('click', function () {
    var aberto = abas.classList.toggle('aberto');
    botao.setAttribute('aria-expanded', aberto ? 'true' : 'false');
  });
})();
