import 'package:flutter/material.dart';

class AppTheme {
  static ThemeData get lightTheme {
    return ThemeData(
      primarySwatch: Colors.blue, // Cor primária: Azul
      hintColor: Colors.lightBlueAccent, // Cor de destaque: Azul claro
      scaffoldBackgroundColor: Colors.white, // Cor de fundo padrão das telas: Branco
      appBarTheme: const AppBarTheme(
        color: Colors.blue, // Cor da AppBar: Azul
        foregroundColor: Colors.white, // Cor do texto e ícones na AppBar: Branco
        centerTitle: true, // Centraliza o título da AppBar
        titleTextStyle: TextStyle(
          color: Colors.white,
          fontSize: 20,
          fontWeight: FontWeight.bold,
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: Colors.blue, // Cor de fundo dos botões elevados: Azul
          foregroundColor: Colors.white, // Cor do texto dos botões elevados: Branco
          padding: const EdgeInsets.symmetric(horizontal: 30, vertical: 15),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
          textStyle: const TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: Colors.blueAccent, // Cor do texto dos botões de texto: Azul claro
        ),
      ),
      // Adicionando um estilo para a InputDecoration (campos de texto)
      inputDecorationTheme: InputDecorationTheme(
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: Colors.blue), // Borda padrão azul
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: Colors.blueAccent), // Borda quando habilitado
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: Colors.blue, width: 2.0), // Borda quando focado
        ),
        labelStyle: const TextStyle(color: Colors.blueGrey), // Cor do label
        hintStyle: const TextStyle(color: Colors.lightBlue), // Cor da dica
      ),
      floatingActionButtonTheme: const FloatingActionButtonThemeData(
        backgroundColor: Colors.blueAccent, // Cor do FloatingActionButton
        foregroundColor: Colors.white, // Cor do ícone do FloatingActionButton
      ),
    );
  }
}