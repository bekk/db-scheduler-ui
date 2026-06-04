# Chakra UI Migration Plan

We are replacing most Chakra UI components with handmade components, but keeping the three that carry complex accessibility behavior.

---

## Replace with handmade components

These are purely presentational — a `div`, `button`, or native form element plus CSS is sufficient.

| Component | Replacement |
|---|---|
| `Box` | `<div>` with CSS / utility classes |
| `HStack` / `VStack` / `Flex` | `<div>` with flexbox CSS |
| `Text` | `<p>` or `<span>` |
| `Divider` | `<hr>` |
| `Button` | `<button>` with styles |
| `IconButton` | `<button>` with an inline icon |
| `Input` | Native `<input>` with styles |
| `Checkbox` | Native `<input type="checkbox">` with styles |
| All icons (`@chakra-ui/icons`) | Inline SVGs or any icon library |

---

## Keep from Chakra (or swap to Radix UI Primitives)

These look simple visually but carry significant invisible behavior that is non-trivial to reimplement correctly — mainly focus management, keyboard navigation, and ARIA wiring.

### `AlertDialog` (used in `ScheduleRunAlert`, `DotButton`, `RunAllAlert`)

Requires:
- Focus trapping — Tab cannot leave the dialog while open
- Body scroll lock
- Focus restored to trigger element on close
- `role="alertdialog"`, `aria-modal`, `aria-labelledby` attributes
- Escape key to close
- Portal rendering outside the main DOM tree

### `Menu` / `MenuButton` / `MenuList` / `MenuItem` (used in `FilterBox`, `DotButton`)

Requires:
- Arrow key navigation between items
- Home / End key support
- Type-ahead character search
- Click-outside to close
- Popover positioning with overflow/flip handling
- Focus management on open and close

### `Accordion` / `AccordionItem` / `AccordionButton` / `AccordionPanel` (used throughout task list and history log)

Requires:
- `aria-expanded`, `aria-controls`, `aria-labelledby` wiring
- Enter / Space to toggle panels
- Arrow key navigation between headers

---

## Alternative: Radix UI Primitives

If we want to drop the Chakra dependency entirely, [Radix UI Primitives](https://www.radix-ui.com/primitives) provides all three of the above headless — correct accessibility behavior with zero default styles, so we can style them however we like.

Relevant packages:
- `@radix-ui/react-dialog` → replaces `AlertDialog`
- `@radix-ui/react-dropdown-menu` → replaces `Menu`
- `@radix-ui/react-accordion` → replaces `Accordion`
