// lib/ui/pages/home_page.dart
import 'package:flutter/material.dart';
import 'package:hackathonflutter/services/auth_service.dart';
import 'package:hackathonflutter/ui/pages/listagem_page.dart';
import 'package:hackathonflutter/ui/pages/login_page.dart';
import 'package:hackathonflutter/screens/camera_screen.dart';
import 'package:provider/provider.dart';

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  /**
   * Método de navegação inteligente (COM A CORREÇÃO DO BUG)
   */
  Future<void> _navegarParaListagem(BuildContext context) async {
    final authService = Provider.of<AuthService>(context, listen: false);
    final String? tipoUsuario = await authService.getTipoUsuario();

    print('DEBUG: O tipo de usuário salvo é: "$tipoUsuario"');

    final ListagemModo modo;

    // --- A CORREÇÃO IMPORTANTE ESTÁ AQUI ---
    // O seu log mostra que o tipo é "ADMIN".
    // Vamos verificar se a string "admin" (em minúsculas) CONTÉM "admin".
    if (tipoUsuario?.toLowerCase().contains('admin') ?? false) {
      modo = ListagemModo.Admin;
    } else {
      modo = ListagemModo.Professor;
    }
    // --- FIM DA CORREÇÃO ---

    if (context.mounted) {
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => ListagemPage(
            modo: modo,
            isViewingGabarito: false,
          ),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final authService = Provider.of<AuthService>(context, listen: false);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Menu Principal'),
        automaticallyImplyLeading: false,
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () => _confirmarLogout(context, authService),
          ),
        ],
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Bem-vindo!',
                style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: Theme.of(context).colorScheme.primary,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Selecione uma opção para começar:',
                style: Theme.of(context).textTheme.titleMedium,
              ),
              const SizedBox(height: 24),

              // --- NOVA UI EM GRELHA (GRID) ---
              Expanded(
                child: GridView.count(
                  crossAxisCount: 2, // Duas colunas
                  crossAxisSpacing: 16,
                  mainAxisSpacing: 16,
                  children: [
                    // Botão 1
                    _MenuCard(
                      context: context,
                      icon: Icons.assignment_turned_in_outlined,
                      text: 'Corrigir Prova', // Texto atualizado
                      onTap: () {
                        _navegarParaListagem(context);
                      },
                    ),

                    // Botão 2
                    _MenuCard(
                      context: context,
                      icon: Icons.camera_alt_outlined,
                      text: 'Escanear Gabarito',
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(builder: (context) => const CameraScreen()),
                        );
                      },
                    ),

                    // Pode adicionar mais cartões aqui
                    // _MenuCard(
                    //   context: context,
                    //   icon: Icons.bar_chart_outlined,
                    //   text: 'Relatórios',
                    //   onTap: () { /* ... */ },
                    // ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  // --- WIDGET REUTILIZÁVEL PARA O CARTÃO DO MENU ---
  Widget _MenuCard({
    required BuildContext context,
    required IconData icon,
    required String text,
    required VoidCallback onTap,
  }) {
    return Card(
      elevation: 4,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Icon(
              icon,
              size: 50,
              color: Theme.of(context).colorScheme.primary,
            ),
            const SizedBox(height: 16),
            Text(
              text,
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  // Método para confirmar o logout (sem alterações)
  void _confirmarLogout(BuildContext context, AuthService authService) {
    // ... (O seu código de _confirmarLogout está perfeito)
    showDialog(
      context: context,
      builder: (BuildContext dialogContext) {
        return AlertDialog(
          title: const Text(
            'Confirmar Saída',
            style: TextStyle(color: Colors.blue),
          ),
          content: const Text(
            'Deseja realmente sair ou trocar de usuário?',
            style: TextStyle(fontSize: 16),
          ),
          actions: <Widget>[
            TextButton(
              child: Text(
                'Cancelar',
                style: TextStyle(
                  color: Colors.grey[600],
                  fontWeight: FontWeight.w500,
                ),
              ),
              onPressed: () {
                Navigator.of(dialogContext).pop();
              },
            ),
            ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.red,
                foregroundColor: Colors.white,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(8),
                ),
              ),
              child: const Text(
                'Sair',
                style: TextStyle(fontWeight: FontWeight.w600),
              ),
              onPressed: () async {
                Navigator.of(dialogContext).pop();
                await authService.logout();
                if (context.mounted) {
                  Navigator.pushReplacement(
                    context,
                    MaterialPageRoute(builder: (context) => const LoginPage()),
                  );
                }
              },
            ),
          ],
        );
      },
    );
  }
}