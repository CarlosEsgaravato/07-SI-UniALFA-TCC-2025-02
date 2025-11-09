import 'package:flutter/material.dart';
import 'package:hackathonflutter/services/auth_service.dart';
import 'package:hackathonflutter/ui/pages/home_page.dart';
import 'package:hackathonflutter/ui/pages/login_page.dart';
import 'package:provider/provider.dart';

class SplashPage extends StatefulWidget {
  const SplashPage({super.key});

  @override
  State<SplashPage> createState() => _SplashPageState();
}

class _SplashPageState extends State<SplashPage> {
  @override
  void initState() {
    super.initState();
    _verificarLogin();
  }

  Future<void> _verificarLogin() async {
    // Aguarda um pouco para mostrar a splash screen
    await Future.delayed(const Duration(seconds: 2));

    // Acessa o AuthService via Provider
    final authService = Provider.of<AuthService>(context, listen: false);

    // Primeiro, verifica se há um token salvo localmente
    final isLocalmenteLogado = await authService.isLogado();

    if (isLocalmenteLogado) {
      // Se houver um token local, tenta validá-lo com a API
      final isValidToken = await authService.isLogado();

      if (mounted) {
        if (isValidToken) {
          // Token válido, redireciona para a HomePage
          Navigator.pushReplacement(
            context,
            MaterialPageRoute(builder: (context) => const HomePage()),
          );
        } else {
          // Token inválido/expirado, força logout e redireciona para LoginPage
          await authService.logout(); // Limpa credenciais inválidas
          if (mounted) {
            Navigator.pushReplacement(
              context,
              MaterialPageRoute(builder: (context) => const LoginPage()),
            );
          }
        }
      }
    } else {
      // Não há token salvo localmente, redireciona para LoginPage
      if (mounted) {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(builder: (context) => const LoginPage()),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [
              Theme.of(context).colorScheme.primary,
              Theme.of(context).colorScheme.secondary,
            ],
          ),
        ),
        child: const Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                Icons.school,
                size: 100,
                color: Colors.white,
              ),
              SizedBox(height: 24),
              Text(
                'Sistema de Avaliação',
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                  color: Colors.white,
                ),
              ),
              SizedBox(height: 16),
              Text(
                'Carregando...',
                style: TextStyle(
                  fontSize: 16,
                  color: Colors.white70,
                ),
              ),
              SizedBox(height: 32),
              CircularProgressIndicator(
                color: Colors.white,
              ),
            ],
          ),
        ),
      ),
    );
  }
}