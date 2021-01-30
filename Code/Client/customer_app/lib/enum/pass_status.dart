enum PassStatus { valid, used, expired }

extension PassStatusExtension on PassStatus {
  String get name {
    switch (this) {
      case PassStatus.expired:
        return 'Expired';
      case PassStatus.used:
        return 'Used';
      case PassStatus.valid:
        return 'Valid';
      default:
        return null;
    }
  }
}

extension PassStatusParser on String {
  PassStatus toPassStatus() {
    return PassStatus.values.firstWhere(
        (e) => e.toString().toLowerCase() == 'passstatus.$this'.toLowerCase(),
        orElse: () => null);
  }
}
