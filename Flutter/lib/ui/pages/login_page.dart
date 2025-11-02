import 'package:flutter/material.dart';
import 'package:hackathonflutter/services/auth_service.dart';
import 'package:hackathonflutter/ui/widgets/barra_titulo.dart';
import 'package:hackathonflutter/ui/widgets/botao_quadrado.dart';
import 'package:hackathonflutter/ui/widgets/campo_texto.dart';
import 'package:hackathonflutter/ui/pages/home_page.dart';
import 'package:hackathonflutter/ui/widgets/msg_alerta.dart';
import 'package:hackathonflutter/ui/widgets/circulo_espera.dart';
// import 'package:hackathonflutter/extensions/string_extension.dart'; // Removido, pois não é mais necessário para validação de email
import 'package:provider/provider.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final TextEditingController _usernameController = TextEditingController(); // Alterado para username
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
    _usernameController.dispose(); // Dispor o novo controller
    _senhaController.dispose();
    super.dispose();
  }

  Future<void> _fazerLogin() async {
    final username = _usernameController.text.trim(); // Alterado para username
    final senha = _senhaController.text.trim();

    if (username.isEmpty || senha.isEmpty) { // Verificação para username
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
      final sucesso = await _authService.autenticar(username, senha); // Chamada com username

      if (mounted) {
        setState(() {
          _carregando = false;
        });

        if (sucesso) {
          Navigator.pushReplacement(
            context,
            MaterialPageRoute(builder: (context) => const HomePage()),
          );
        } else {
          MsgAlerta.show(
            context: context,
            titulo: 'Erro de Login',
            texto: 'Usuário ou senha inválidos. Tente novamente.',
          );
        }
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _carregando = false;
        });

        MsgAlerta.show(
          context: context,
          titulo: 'Erro',
          texto: 'Ocorreu um erro inesperado. Tente novamente mais tarde. Erro: $e',
        );
      }
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
              // Ícone ou Logo
              Icon(
                Icons.person_pin,
                size: 100,
                color: Theme.of(context).colorScheme.primary,
              ),
              const SizedBox(height: 48),

              // Campo de Usuário
              CampoTexto(
                controller: _usernameController, // Usando _usernameController
                texto: 'Usuário', // Texto do label alterado
                teclado: TextInputType.text, // Pode ser text, emailAddress, etc., dependendo do seu username
                onChanged: (text) => setState(() {}),
              ),
              const SizedBox(height: 16),

              // Campo de Senha
              CampoTexto(
                controller: _senhaController,
                texto: 'Senha',
                isObscureText: true,
                onChanged: (text) => setState(() {}),
              ),
              const SizedBox(height: 32),

              // Botão de Login
              BotaoQuadrado(
                clique: _fazerLogin,
                texto: 'Entrar',
                icone: Icons.login,
              ),
              const SizedBox(height: 20),

              // Texto ou Link para Recuperação de Senha (Opcional)
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