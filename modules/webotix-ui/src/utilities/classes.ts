/** Return CSS class for alignment. */
import {Alignment} from "./alignment";
import {Intent} from "./intent";

export function alignmentClass(alignment?: Alignment) {
    switch (alignment) {
        case Alignment.left:
            return 'align-left';
        case Alignment.right:
            return 'align-right';
        case Alignment.center:
            return 'align-center';
        default:
            return undefined;
    }
}

/** Returns CSS class for icon name. */
export function iconClass(iconName?: string) {
    if (iconName == null) {
        return undefined;
    }
    return iconName.indexOf(`icon-`) === 0 ? iconName : `icon-${iconName}`;
}

/** Return CSS class for intent. */
export function intentClass(intent?: Intent) {
    if (intent == null || intent === Intent.none) {
        return undefined;
    }
    return `intent-${intent.toLowerCase()}`;
}

export function positionClass(position: Position) {
    if (position == null) {
        return undefined;
    }
    return `position-${position}`;
}
