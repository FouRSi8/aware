"""Render the exported Room schema as a reviewable PNG diagram."""

from __future__ import annotations

import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
SCHEMA = ROOT / "app/schemas/com.aware.app.data.AppDatabase/6.json"
OUTPUT = ROOT / "docs/media/database-schema-v6.png"

WIDTH, HEIGHT = 3200, 3000
BACKGROUND = "#101416"
SURFACE = "#192024"
SURFACE_ALT = "#20292E"
TEXT = "#F7F2EA"
MUTED = "#AEB9BE"
BORDER = "#3A484F"
PRIMARY = "#C9DDAA"
EXPENSE = "#D9A08F"
INCOME = "#9CC7A3"
LOGICAL = "#D7BB82"

FONT_REGULAR = Path("C:/Windows/Fonts/segoeui.ttf")
FONT_SEMIBOLD = Path("C:/Windows/Fonts/seguisb.ttf")
FONT_MONO = Path("C:/Windows/Fonts/consola.ttf")


def font(path: Path, size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(str(path), size)


TITLE = font(FONT_SEMIBOLD, 64)
SUBTITLE = font(FONT_REGULAR, 30)
TABLE_TITLE = font(FONT_SEMIBOLD, 34)
FIELD = font(FONT_MONO, 24)
SMALL = font(FONT_REGULAR, 22)
LEGEND = font(FONT_REGULAR, 25)


def dashed(draw: ImageDraw.ImageDraw, points: list[tuple[int, int]], fill: str, width: int = 4) -> None:
    for start, end in zip(points, points[1:]):
        x1, y1 = start
        x2, y2 = end
        length = max(abs(x2 - x1), abs(y2 - y1))
        if length == 0:
            continue
        steps = max(1, length // 18)
        for step in range(0, steps, 2):
            a = step / steps
            b = min(1.0, (step + 1) / steps)
            draw.line((x1 + (x2 - x1) * a, y1 + (y2 - y1) * a,
                       x1 + (x2 - x1) * b, y1 + (y2 - y1) * b), fill=fill, width=width)


def arrow(draw: ImageDraw.ImageDraw, points: list[tuple[int, int]], fill: str, logical: bool, label: str) -> None:
    if logical:
        dashed(draw, points, fill)
    else:
        draw.line(points, fill=fill, width=5, joint="curve")
    x, y = points[-1]
    draw.polygon([(x, y), (x - 16, y - 10), (x - 16, y + 10)], fill=fill)


def main() -> None:
    payload = json.loads(SCHEMA.read_text(encoding="utf-8"))["database"]
    entities = {entity["tableName"]: entity for entity in payload["entities"]}
    positions = {
        "expense_categories": (70, 250),
        "income_categories": (70, 690),
        "accounts": (70, 1130),
        "savings_goals": (70, 1510),
        "transactions": (1140, 250),
        "merchant_rules": (1140, 1120),
        "monthly_plans": (1140, 1590),
        "budget_buckets": (2230, 250),
        "recurring_rules": (2230, 1090),
    }
    box_width = 900
    row_height = 42
    boxes: dict[str, tuple[int, int, int, int]] = {}
    for name, (x, y) in positions.items():
        height = 112 + len(entities[name]["fields"]) * row_height
        boxes[name] = (x, y, x + box_width, y + height)

    image = Image.new("RGB", (WIDTH, HEIGHT), BACKGROUND)
    draw = ImageDraw.Draw(image)
    draw.text((70, 56), "aware database schema", font=TITLE, fill=TEXT)
    draw.text((70, 132), "Room v6 · encrypted locally · monetary values stored as integer paise", font=SUBTITLE, fill=MUTED)

    def right(name: str, ratio: float = .5) -> tuple[int, int]:
        x1, y1, x2, y2 = boxes[name]
        return x2, int(y1 + (y2 - y1) * ratio)

    def left(name: str, ratio: float = .5) -> tuple[int, int]:
        x1, y1, _, y2 = boxes[name]
        return x1, int(y1 + (y2 - y1) * ratio)

    # Solid lines are the two database-enforced foreign keys.
    arrow(draw, [right("accounts", .35), (1080, right("accounts", .35)[1]), (1080, left("transactions", .38)[1]), left("transactions", .38)], PRIMARY, False, "accountId")
    arrow(draw, [right("accounts", .55), (1110, right("accounts", .55)[1]), (1110, left("transactions", .45)[1]), left("transactions", .45)], PRIMARY, False, "destinationAccountId")

    # Dashed relations are validated by repository rules because category IDs span two tables.
    arrow(draw, [right("expense_categories", .55), (1050, right("expense_categories", .55)[1]), (1050, left("transactions", .53)[1]), left("transactions", .53)], LOGICAL, True, "expense categoryId")
    arrow(draw, [right("income_categories", .55), (1010, right("income_categories", .55)[1]), (1010, left("transactions", .60)[1]), left("transactions", .60)], LOGICAL, True, "income categoryId")
    arrow(draw, [right("transactions", .70), (2180, right("transactions", .70)[1]), (2180, left("budget_buckets", .60)[1]), left("budget_buckets", .60)], LOGICAL, True, "expense totals")
    arrow(draw, [right("transactions", .82), (2160, right("transactions", .82)[1]), (2160, left("recurring_rules", .46)[1]), left("recurring_rules", .46)], LOGICAL, True, "materialises expected")
    arrow(draw, [right("merchant_rules", .45), (2180, right("merchant_rules", .45)[1]), (2180, 1160), (1090, 1160), (1090, left("transactions", .66)[1]), left("transactions", .66)], LOGICAL, True, "suggests mapping")

    for name, entity in entities.items():
        x1, y1, x2, y2 = boxes[name]
        accent = EXPENSE if name == "expense_categories" else INCOME if name == "income_categories" else PRIMARY
        draw.rounded_rectangle((x1, y1, x2, y2), 24, fill=SURFACE, outline=BORDER, width=3)
        draw.rounded_rectangle((x1, y1, x2, y1 + 78), 24, fill=SURFACE_ALT)
        draw.rectangle((x1, y1 + 52, x2, y1 + 78), fill=SURFACE_ALT)
        draw.rectangle((x1, y1, x1 + 10, y1 + 78), fill=accent)
        draw.text((x1 + 30, y1 + 19), name, font=TABLE_TITLE, fill=TEXT)

        primary = set(entity["primaryKey"]["columnNames"])
        indexed = {column for index in entity.get("indices", []) for column in index["columnNames"]}
        foreign = {column for fk in entity.get("foreignKeys", []) for column in fk["columns"]}
        for index, field_data in enumerate(entity["fields"]):
            field_name = field_data["columnName"]
            markers = []
            if field_name in primary:
                markers.append("PK")
            if field_name in foreign:
                markers.append("FK")
            if field_name in indexed:
                markers.append("IDX")
            marker = " · ".join(markers) or "   "
            nullable = "" if field_data.get("notNull", False) else "?"
            type_name = field_data["affinity"] + nullable
            row_y = y1 + 95 + index * row_height
            draw.text((x1 + 28, row_y), marker.ljust(10), font=FIELD, fill=accent if markers else MUTED)
            draw.text((x1 + 190, row_y), field_name, font=FIELD, fill=TEXT)
            type_box = draw.textbbox((0, 0), type_name, font=FIELD)
            draw.text((x2 - 28 - (type_box[2] - type_box[0]), row_y), type_name, font=FIELD, fill=MUTED)

    relation_x, relation_y = 1140, 2050
    draw.rounded_rectangle((relation_x, relation_y, relation_x + 900, relation_y + 575), 24, fill=SURFACE, outline=BORDER, width=3)
    draw.text((relation_x + 28, relation_y + 22), "relationship rules", font=TABLE_TITLE, fill=TEXT)
    relationships = [
        (PRIMARY, "ENFORCED  transactions.accountId → accounts.id  · RESTRICT"),
        (PRIMARY, "ENFORCED  transactions.destinationAccountId → accounts.id  · SET NULL"),
        (LOGICAL, "LOGICAL   transactions.categoryId → category table selected by type"),
        (LOGICAL, "LOGICAL   budget_buckets.categoryId → expense_categories.id"),
        (LOGICAL, "LOGICAL   recurring_rules.categoryId → category table selected by type"),
        (LOGICAL, "LOGICAL   merchant_rules.categoryId → globally unique category id"),
        (LOGICAL, "PLAN      monthly_plans protects savings and commitments by month"),
        (LOGICAL, "GOAL      savings_goals tracks user-defined progress targets"),
    ]
    for index, (color, relationship) in enumerate(relationships):
        y = relation_y + 92 + index * 55
        draw.ellipse((relation_x + 30, y + 8, relation_x + 42, y + 20), fill=color)
        draw.text((relation_x + 58, y), relationship, font=SMALL, fill=MUTED)

    legend_y = HEIGHT - 85
    draw.line((70, legend_y, 135, legend_y), fill=PRIMARY, width=5)
    draw.text((150, legend_y - 16), "enforced foreign key", font=LEGEND, fill=MUTED)
    dashed(draw, [(520, legend_y), (585, legend_y)], LOGICAL)
    draw.text((600, legend_y - 16), "logical repository relation", font=LEGEND, fill=MUTED)
    draw.text((1180, legend_y - 16), "PK primary key   ·   FK foreign key   ·   IDX indexed   ·   ? nullable", font=LEGEND, fill=MUTED)

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    image.save(OUTPUT, optimize=True)
    print(OUTPUT)


if __name__ == "__main__":
    main()
