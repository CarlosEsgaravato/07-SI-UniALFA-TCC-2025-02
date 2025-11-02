// lib/ui/pages/home_page.dart
import 'package:flutter/material.dart';
import 'package:hackathonflutter/services/auth_service.dart';
import 'package:hackathonflutter/ui/pages/listagem_page.dart';
import 'package:hackathonflutter/ui/pages/login_page.dart';
import 'package:hackathonflutter/screens/camera_screen.dart'; // Importe CameraScreen
import 'package:hackathonflutter/ui/widgets/botao_quadrado.dart'; // Importe BotaoQuadrado
import 'package:provider/provider.dart'; // Importar o pacote provider

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    final screenWidth = MediaQuery.of(context).size.width;
    final screenHeight = MediaQuery.of(context).size.height;


    final authService = Provider.of<AuthService>(context, listen: false);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Menu Principal'),
        automaticallyImplyLeading: false, // Remove o botão de voltar padrão
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () => _confirmarLogout(context, authService),
          ),
        ],
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          child: Padding(
            padding: EdgeInsets.symmetric(
              horizontal: screenWidth * 0.05, // 5% das bordas
              vertical: screenHeight * 0.03, // 3% superior/inferior
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Adicione aqui uma imagem ou logo se desejar
                SizedBox(height: screenHeight * 0.02),
                Text(
                  'Bem-vindo ao Sistema de Avaliação!',
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                  textAlign: TextAlign.center,
                ),
                SizedBox(height: screenHeight * 0.05),

                // Botão para Preencher Gabarito
                BotaoQuadrado(
                  icone: Icons.assignment,
                  texto: 'Preencher Gabarito',
                  clique: () {

                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const ListagemPage(isViewingGabarito: false),
                      ),
                    );
                  },
                ),
                SizedBox(height: screenHeight * 0.03),

                // NOVO BOTÃO: Visualizar Gabaritos
                // BotaoQuadrado(
                //   icone: Icons.check_circle_outline, // Ícone para gabarito correto
                //   texto: 'Visualizar Gabaritos',
                //   clique: () {
                //     // Navega para a ListagemPage no modo de visualização de gabarito
                //     Navigator.push(
                //       context,
                //       MaterialPageRoute(
                //         builder: (context) => const ListagemPage(isViewingGabarito: true),
                //       ),
                //     );
                //   },
                // ),
                // SizedBox(height: screenHeight * 0.03), // Espaçamento
                //
                // Botão para Escanear Gabarito (se ainda for usado)
                BotaoQuadrado(
                  icone: Icons.camera_alt,
                  texto: 'Escanear Gabarito',
                  clique: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(builder: (context) => const CameraScreen()),
                    );
                  },
                ),
                SizedBox(height: screenHeight * 0.03), // Espaçamento

                // Outros botões ou informações podem vir aqui
              ],
            ),
          ),
        ),
      ),
    );
  }

  void _confirmarLogout(BuildContext context, AuthService authService) {
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
                await authService.logout(); // Chama o logout do AuthService injetado
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