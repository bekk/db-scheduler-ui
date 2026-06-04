# Chakra Migration Issues

---

## Add flex layout utility classes

Box, HStack, VStack, and Flex are Chakra wrappers around flexbox — they add no semantic meaning.

**Replaces:** `Box` (when used as a flex container), `HStack`, `VStack`, `Flex`  
**How:** Create `src/styles/layout.css` with a small set of utility classes: `.row` (flex row, align-items center), `.col` (flex column), `.gap-sm / .gap-md / .gap-lg`, `.align-center`, `.justify-between`, `.justify-end`. Replace Chakra flex containers with `<div className="row gap-md">` etc. Non-flex `Box` usage (borders, padding, background color) stays as inline `style={{}}`.  
**Where:** `src/styles/layout.css`; update all files importing `Box`/`HStack`/`VStack`/`Flex` from `@chakra-ui/react`.

---

## Replace Text with native HTML elements

`Text` is a Chakra wrapper around `<p>` and `<span>` with no behaviour of its own.

**Replaces:** `Text` from `@chakra-ui/react`  
**How:** Swap `<Text>` for `<p>` or `<span>`. Carry over font weight, color, and size as inline `style={{}}`. Note: `TopBar.tsx` uses `<Text as="button">` — replace that with a plain `<button>`.  
**Where:** `TopBar.tsx`, `SortButton.tsx`, `LogAccordionItem.tsx`, `LogList.tsx`

---

## Replace Divider with hr

`Divider` renders a styled horizontal rule with no extra behaviour.

**Replaces:** `Divider` from `@chakra-ui/react`  
**How:** Replace `<Divider />` with `<hr style={{ borderColor: colors.primary[300], borderTopWidth: 1 }} />`.  
**Where:** `src/components/history/LogAccordionItem.tsx`

---

## Add shared Button component

Action buttons (Run/Rerun), the Refresh button, and the nav tabs in TopBar all use Chakra's `Button` with different inline style combinations. A shared component with variants removes the Chakra dependency and makes styles consistent.

**Replaces:** Chakra `Button` and `IconButton`  
**How:** Create `src/components/common/Button.tsx` — a native `<button>` with a `variant` prop (`"primary" | "secondary" | "nav"`), an optional `icon` prop (React node), and an optional `iconSide` prop (`"left" | "right"`, defaults to `"left"`). Define all variant styles in `src/styles/button.css`. Replace all Chakra `Button`/`IconButton` usages. Task-status-specific colours in `TaskRunButton` can pass a `style` prop override.  
**Where:** `src/components/common/Button.tsx` + `src/styles/button.css`; update `TaskRunButton.tsx`, `RefreshButton.tsx`, `PaginationButtons.tsx`, `TopBar.tsx`, `TaskList.tsx`, `LogList.tsx`.

---

## Replace Input with native input

Chakra's `Input` is a styled text field used in HeaderBar for task name and task instance search.

**Replaces:** `Input` from `@chakra-ui/react`  
**How:** Replace `<Input />` with `<input type="text" className="search-input" />` and add `.search-input` styles (border, padding, border-radius, background) to `src/styles/input.css`.  
**Where:** `src/components/common/HeaderBar.tsx`

---

## Replace Checkbox with native checkbox

Chakra's `Checkbox` is used in HeaderBar for the "exact match" toggles on the task name and task instance search fields.

**Replaces:** `Checkbox` from `@chakra-ui/react`  
**How:** Replace `<Checkbox>label</Checkbox>` with `<label><input type="checkbox" className="checkbox" /> label</label>` and add `.checkbox` styles to `src/styles/input.css`.  
**Where:** `src/components/common/HeaderBar.tsx`

---

## Replace @chakra-ui/icons with inline SVG components

All icons imported from `@chakra-ui/icons` are used as sized inline graphics. The codebase already has a pattern of custom SVG components in `src/assets/icons/`.

**Replaces:** `@chakra-ui/icons` (ChevronDownIcon, RepeatIcon, ArrowRightIcon, AccordionIcon, etc.)  
**How:** For each icon currently imported from `@chakra-ui/icons`, add a matching SVG component in `src/assets/icons/` following the existing pattern. Accept `width` / `height` props (or a `className`) for sizing. Replace all `@chakra-ui/icons` imports across the codebase.  
**Where:** `src/assets/icons/`; update `RefreshButton.tsx`, `FilterBox.tsx`, `TaskRunButton.tsx`, `LogAccordionButton.tsx`, and any other file importing from `@chakra-ui/icons`.

---

## Swap AlertDialog to Radix Dialog

`AlertDialog` is used for confirmation modals and requires focus trapping, scroll lock, ARIA attributes, portal rendering, and Escape-to-close — non-trivial to reimplement correctly.

**Replaces:** Chakra `AlertDialog` and related sub-components (`AlertDialogOverlay`, `AlertDialogContent`, `AlertDialogHeader`, `AlertDialogBody`, `AlertDialogFooter`)  
**How:** Install `@radix-ui/react-dialog`. Replace Chakra AlertDialog with `Dialog.Root`, `Dialog.Portal`, `Dialog.Overlay`, `Dialog.Content`, `Dialog.Title`, `Dialog.Description`. Style via CSS to match current appearance.  
**Where:** `src/components/input/ScheduleRunAlert.tsx`, `src/components/input/DotButton.tsx`, `src/components/scheduled/RunAllAlert.tsx`

---

## Swap Menu to Radix Dropdown Menu

`Menu`/`MenuButton`/`MenuList`/`MenuItem` are used for the filter dropdown and task action menu. Arrow-key navigation, type-ahead, click-outside, and popover positioning make this non-trivial to hand-roll.

**Replaces:** Chakra `Menu`, `MenuButton`, `MenuList`, `MenuItem`  
**How:** Install `@radix-ui/react-dropdown-menu`. Replace with `DropdownMenu.Root`, `DropdownMenu.Trigger`, `DropdownMenu.Content`, `DropdownMenu.Item`. Style via CSS to match current appearance.  
**Where:** `src/components/input/FilterBox.tsx`, `src/components/input/DotButton.tsx`

---

## Swap Accordion to Radix Accordion

`Accordion` is used to expand/collapse task cards and log entries throughout the app. Correct ARIA wiring (`aria-expanded`, `aria-controls`) and keyboard navigation (Enter/Space, arrow keys) are already handled by the library.

**Replaces:** Chakra `Accordion`, `AccordionItem`, `AccordionButton`, `AccordionPanel`, `AccordionIcon`  
**How:** Install `@radix-ui/react-accordion`. Replace with `Accordion.Root`, `Accordion.Item`, `Accordion.Trigger`, `Accordion.Content`. Style the open/close animation and chevron icon via CSS. Remove the `AccordionIcon` import and substitute with an SVG from `src/assets/icons/`.  
**Where:** `src/components/history/LogAccordionItem.tsx`, `src/components/history/LogAccordionButton.tsx`, `src/components/scheduled/TaskAccordionItem.tsx`, `src/components/scheduled/TaskAccordionButton.tsx`
