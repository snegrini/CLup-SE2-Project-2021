/// DayOfWeek is an enum representing the 7 days of the week.
/// The int value follows the ISO-8601 standard, from 1 (Monday) to 7 (Sunday).
enum DayOfWeek {
  monday,
  tuesday,
  wednesday,
  thursday,
  friday,
  saturday,
  sunday
}

final List<DayOfWeek> _enums = DayOfWeek.values;

class DayOfWeekHelper {
  /// Obtains an instance of DayOfWeek from an int value.
  /// This factory allows the enum to be obtained from the int value.
  static DayOfWeek of(int dayOfWeek) {
    if (dayOfWeek < 1 || dayOfWeek > 7) {
      throw Exception("Invalid value for DayOfWeek: " + dayOfWeek.toString());
    }
    return _enums.elementAt(dayOfWeek - 1);
  }
}

extension DayOfWeekExtension on DayOfWeek {
  String get name {
    return this.toShortString().capitalize();
  }

  String toShortString() {
    return this.toString().split('.').last;
  }
}

extension StringExtension on String {
  String capitalize() {
    return "${this[0].toUpperCase()}${this.substring(1)}";
  }
}