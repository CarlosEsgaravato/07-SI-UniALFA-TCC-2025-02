import 'package:flutter/material.dart';
import 'package:hackathonflutter/services/auth_service.dart';
import 'package:hackathonflutter/ui/widgets/barra_titulo.dart';
import 'package:hackathonflutter/ui/widgets/botao_quadrado.dart';
import 'package:hackathonflutter/ui/widgets/campo_texto.dart';
import 'package:hackathonflutter/ui/pages/home_page.dart';
import 'package:hackathonflutter/ui/widgets/msg_alerta.dart';
import 'package:hackathonflutter/ui/widgets/circulo_espera.dart';
import 'package:provider/provider.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final TextEditingController _usernameController = TextEditingController();
  final TextEditingController _senhaController = TextEditingController();
  late AuthService _authService;
  bool _carregando = false;

  @override
  void initState() {
    super.initState();
    _authService = Provider.of<AuthService>(context, listen: false);
  }

  @override
  void dispose() {
    _usernameController.dispose();
    _senhaController.dispose();
    super.dispose();
  }

  Future<void> _fazerLogin() async {
    final username = _usernameController.text.trim();
    final senha = _senhaController.text.trim();

    if (username.isEmpty || senha.isEmpty) {
      MsgAlerta.show(
        context: context,
        titulo: 'Campos Obrigatórios',
        texto: 'Por favor, preencha todos os campos.',
      );
      return;
    }

    setState(() {
      _carregando = true;
    });

    try {
      final String? erro = await _authService.autenticar(username, senha);

      if (!mounted) return;

      setState(() {
        _carregando = false;
      });

      if (erro == null) {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(builder: (context) => const HomePage()),
        );
      } else {
        MsgAlerta.show(
          context: context,
          titulo: 'Erro de Login',
          texto: erro,
        );
      }
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _carregando = false;
      });
      MsgAlerta.show(
        context: context,
        titulo: 'Erro Inesperado',
        texto: 'Ocorreu um erro. Tente novamente mais tarde. Erro: $e',
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: BarraTitulo.criar('Login'),
      body: _carregando
          ? const CirculoEspera()
          : Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              Icon(
                Icons.person_pin,
                size: 100,
                color: Theme.of(context).colorScheme.primary,
              ),
              const SizedBox(height: 48),

              CampoTexto(
                controller: _usernameController,
                texto: 'Usuário',
                teclado: TextInputType.text,
                onChanged: (text) => setState(() {}),
              ),
              const SizedBox(height: 16),

              CampoTexto(
                controller: _senhaController,
                texto: 'Senha',
                isObscureText: true,
                onChanged: (text) => setState(() {}),
              ),
              const SizedBox(height: 32),

              BotaoQuadrado(
                clique: _fazerLogin,
                texto: 'Entrar',
                icone: Icons.login,
              ),
              const SizedBox(height: 20),

              TextButton(
                onPressed: () {
                  MsgAlerta.show(
                    context: context,
                    titulo: 'Esqueceu a Senha?',
                    texto: 'Entre em contato com o suporte para recuperar sua senha.',
                  );
                },
                child: const Text('Esqueceu sua senha?'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}