extension StringExtension on String {
  String capitalize() {
    if (isEmpty) return this;
    return this[0].toUpperCase() + substring(1).toLowerCase();
  }

  String get initials {
    if (isEmpty) return '';
    List<String> words = split(' ');
    if (words.length == 1) {
      return words[0][0].toUpperCase();
    }
    return '${words.first[0]}${words.last[0]}'.toUpperCase();
  }

  bool get isValidEmail {
    return RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$').hasMatch(this);
        }
}