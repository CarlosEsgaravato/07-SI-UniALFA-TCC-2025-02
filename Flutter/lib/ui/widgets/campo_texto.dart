import 'package:flutter/material.dart';

class CampoTexto extends StatelessWidget {
  final TextEditingController controller;
  final String? texto;
  final TextInputType? teclado;
  final bool? isHabilitado;
  final bool? isObscureText;
  final ValueChanged<String>? onChanged; // Adicionar esta propriedade

  const CampoTexto({
    required this.controller,
    this.texto,
    this.teclado,
    this.isHabilitado,
    this.isObscureText = false,
    this.onChanged, // Inicializar no construtor
    super.key
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16), // Padding ajustado para const
      child: TextField(
        enabled: isHabilitado,
        controller: controller,
        keyboardType: teclado,
        obscureText: isObscureText!,
        onChanged: onChanged, // Atribuir o callback
        decoration: InputDecoration(
            labelText: texto,
            border: const OutlineInputBorder()
        ),
      ),
    );
  }
}