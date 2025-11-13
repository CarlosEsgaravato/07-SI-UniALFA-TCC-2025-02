/*
 * =========================================
 * Sidebar Toggle - SOLUÇÃO FOUC/FOIC
 * =========================================
 * ESTRATÉGIA: O JavaScript NÃO define o estado inicial.
 * O CSS já cuida disso via media queries.
 * O JS apenas gerencia as interações do usuário (cliques).
 */

document.addEventListener('DOMContentLoaded', function() {
    const sidebar = document.querySelector('.sidebar');
    const mainContent = document.getElementById('main-content');
    const menuOpenToggle = document.getElementById('menu-open-toggle');
    const menuCloseToggle = document.getElementById('menu-close-toggle');

    /**
     * Listener para o botão de abrir (hamburguer no mobile)
     * Remove a classe collapsed para mostrar a sidebar
     */
    if (menuOpenToggle) {
        menuOpenToggle.addEventListener('click', function() {
            sidebar.classList.remove('collapsed');
        });
    }

    /**
     * Listener para o botão de fechar (X dentro da sidebar no mobile)
     * Adiciona a classe collapsed para esconder a sidebar
     */
    if (menuCloseToggle) {
        menuCloseToggle.addEventListener('click', function() {
            sidebar.classList.add('collapsed');
        });
    }

    /**
     * NOVO: Fecha a sidebar automaticamente ao clicar em qualquer link no mobile
     * Melhora a UX evitando que o usuário precise fechar manualmente
     */
    const sidebarLinks = sidebar.querySelectorAll('a');
    sidebarLinks.forEach(function(link) {
        link.addEventListener('click', function() {
            // Fecha apenas no mobile
            if (window.innerWidth < 992) {
                sidebar.classList.add('collapsed');
            }
        });
    });

    /**
     * Handler de resize para garantir consistência ao mudar de orientação/tamanho
     * ATUALIZADO: Agora remove a classe 'collapsed' ao voltar para desktop
     */
    let resizeTimeout;
    window.addEventListener('resize', function() {
        // Debounce para evitar múltiplas execuções durante o resize
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(function() {
            const isDesktop = window.innerWidth >= 992;
            const isMobile = window.innerWidth < 992;

            if (isDesktop) {
                // Desktop: Remove a classe collapsed para garantir que o menu apareça
                sidebar.classList.remove('collapsed');
                // Remove também a classe expanded do main-content
                mainContent.classList.remove('expanded');
            } else if (isMobile) {
                // Mobile: Garante que o menu fecha ao redimensionar para mobile
                sidebar.classList.add('collapsed');
                // Remove a classe expanded (não usada no mobile)
                mainContent.classList.remove('expanded');
            }
        }, 150); // 150ms de debounce
    });
});