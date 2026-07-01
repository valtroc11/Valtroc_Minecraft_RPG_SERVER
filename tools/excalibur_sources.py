from __future__ import annotations

from pathlib import Path
import zipfile


ROOT = Path(__file__).resolve().parents[1]


def find_excalibur_zip() -> Path:
    candidates = sorted(ROOT.glob("Excalibur*.zip"), key=lambda path: path.stat().st_mtime, reverse=True)
    if not candidates:
        raise FileNotFoundError("No se encontro un ZIP de Excalibur en la raiz del proyecto.")
    return candidates[0]


def ensure_excalibur_texture(zip_member: str, destination: Path) -> Path:
    destination.parent.mkdir(parents=True, exist_ok=True)
    zip_path = find_excalibur_zip()
    with zipfile.ZipFile(zip_path) as archive:
        try:
            data = archive.read(zip_member)
        except KeyError as exc:
            raise FileNotFoundError(f"No existe {zip_member} dentro de {zip_path.name}.") from exc
    destination.write_bytes(data)
    return destination
