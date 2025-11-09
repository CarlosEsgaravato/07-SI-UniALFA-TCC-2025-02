// lib/ui/widgets/botao_flutuante.dart
import 'package:flutter/material.dart';

class BotaoFlutuante extends StatelessWidget {
  final IconData icone;
  final VoidCallback? evento; // <<-- MUITO IMPORTANTE: Torna o evento anulável

  const BotaoFlutuante({
    super.key,
    required this.icone,
    this.evento, // Pode ser null para desabilitar
  });

  @override
  Widget build(BuildContext context) {
    return FloatingActionButton(
      onPressed: evento, // Quando evento é null, o botão é desabilitado
      child: Icon(icone),
    );
  }
}