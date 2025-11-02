// lib/ui/widgets/msg_alerta.dart
import 'package:flutter/material.dart';

class MsgAlerta {
  // Construtor privado para evitar instanciação, já que será uma classe estática
  MsgAlerta._();

  static Future<void> show({
    required BuildContext context,
    required String titulo,
    required String texto,
    List<Widget>? botoes,
    Color? titleColor,
  }) async {
    await showDialog(
      context: context,
      builder: (BuildContext dialogContext) { // Use dialogContext para o Navigator.pop
        return AlertDialog(
          title: Text(titulo, style: TextStyle(color: titleColor)),
          content: Text(texto),
          actions: _criarBotoes(dialogContext, botoes),
        );
      },
    );
  }

  static List<Widget> _criarBotoes(BuildContext context, List<Widget>? botoes) {
    if (botoes == null) {
      return [
        TextButton(
          onPressed: () {
            Navigator.pop(context);
          },
          child: const Text('Ok'),
        )
      ];
    }
    return botoes;
  }

  // --- Métodos de conveniência ---

  static Future<void> showError(BuildContext context, String titulo, String texto) async {
    await show(
      context: context,
      titulo: titulo,
      texto: texto,
      titleColor: Colors.red,
      botoes: [
        TextButton(
          onPressed: () {
            Navigator.pop(context);
          },
          child: const Text('Ok'),
        )
      ],
    );
  }

  static Future<void> showSuccess(BuildContext context, String titulo, String texto) async {
    await show(
      context: context,
      titulo: titulo,
      texto: texto,
      titleColor: Colors.green,
      botoes: [
        TextButton(
          onPressed: () {
            Navigator.pop(context);
          },
          child: const Text('Ok'),
        )
      ],
    );
  }

  static Future<void> showWarning(BuildContext context, String titulo, String texto) async {
    await show(
      context: context,
      titulo: titulo,
      texto: texto,
      titleColor: Colors.orange,
      botoes: [
        TextButton(
          onPressed: () {
            Navigator.pop(context);
          },
          child: const Text('Ok'),
        )
      ],
    );
  }

  static Future<void> showInfo(BuildContext context, String titulo, String texto) async {
    await show(
      context: context,
      titulo: titulo,
      texto: texto,
      titleColor: Colors.blue, // Cor azul para informações
      botoes: [
        TextButton(
          onPressed: () {
            Navigator.pop(context);
          },
          child: const Text('Ok'),
        )
      ],
    );
  }

  static Future<bool> showConfirm(BuildContext context, String titulo, String texto) async {
    bool? confirm = await showDialog<bool>(
      context: context,
      builder: (BuildContext dialogContext) {
        return AlertDialog(
          title: Text(titulo, style: const TextStyle(color: Colors.blue)),
          content: Text(texto),
          actions: <Widget>[
            TextButton(
              onPressed: () {
                Navigator.of(dialogContext).pop(false); // Retorna false para "Cancelar"
              },
              child: const Text('Cancelar'),
            ),
            TextButton(
              onPressed: () {
                Navigator.of(dialogContext).pop(true); // Retorna true para "Confirmar"
              },
              child: const Text('Confirmar'),
            ),
          ],
        );
      },
    );
    return confirm ?? false; // Retorna false se o diálogo for descartado (clicando fora, por exemplo)
  }
}