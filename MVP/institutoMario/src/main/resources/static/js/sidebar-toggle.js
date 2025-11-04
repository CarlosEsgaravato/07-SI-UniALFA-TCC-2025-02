document.addEventListener('DOMContentLoaded', function() {
    const sidebar = document.querySelector('.sidebar');
    const mainContent = document.getElementById('main-content');
    const menuOpenToggle = document.getElementById('menu-open-toggle');
    const menuCloseToggle = document.getElementById('menu-close-toggle');

    // Função para alternar o estado do menu
    function toggleSidebar() {
        // Alterna a classe 'collapsed' na sidebar
        sidebar.classList.toggle('collapsed');

        // Em telas maiores (Desktop), alterna a margem do conteúdo principal
        if (window.innerWidth >= 992) {
            mainContent.classList.toggle('expanded');
        }
    }

    // Listener para o botão de abrir (hamburguer, no main-content)
    if (menuOpenToggle) {
        menuOpenToggle.addEventListener('click', toggleSidebar);
    }

    // Listener para o botão de fechar (X, dentro do sidebar)
    if (menuCloseToggle) {
        menuCloseToggle.addEventListener('click', toggleSidebar);
    }

    // Inicializa o estado do menu corretamente para desktop/mobile no carregamento e redimensionamento
    function handleResize() {
        // Se a largura for de desktop (>= 992px)
        if (window.innerWidth >= 992) {
            // Garante que o menu está visível e o conteúdo principal tem o margin
            sidebar.classList.remove('collapsed');
            mainContent.classList.remove('expanded');
        } else {
            // Mobile: Garante que o menu está colapsado (escondido)
            sidebar.classList.add('collapsed');
            mainContent.classList.remove('expanded');
        }
    }

    // Chama no carregamento e na mudança de tamanho da tela
    handleResize();
    window.addEventListener('resize', handleResize);
});
