import 'package:flutter/material.dart';

class BotaoFlutuante extends StatelessWidget {
  final IconData icone;
  final VoidCallback? evento;

  const BotaoFlutuante({
    super.key,
    required this.icone,
    this.evento,
  });

  @override
  Widget build(BuildContext context) {
    return FloatingActionButton(
      onPressed: evento,
      child: Icon(icone),
    );
  }
}