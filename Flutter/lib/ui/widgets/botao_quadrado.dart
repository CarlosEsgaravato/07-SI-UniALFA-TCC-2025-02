import 'package:flutter/material.dart';

class BotaoQuadrado extends StatelessWidget {
  final VoidCallback clique;
  final String texto;
  final IconData? icone;

  const BotaoQuadrado({required this.clique,
    required this.texto, this.icone, super.key});

  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      onPressed: clique,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          _criarIcone(),
          Text(texto)
        ],
      ),
    );
  }

  Widget _criarIcone() {
    if (icone != null) {
      return Padding(
        padding: EdgeInsets.only(right: 4),
        child: Icon(icone),
      );
    }
    return SizedBox();
  }
}