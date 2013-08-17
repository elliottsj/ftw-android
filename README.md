Welcome
=============

![Screenshot](https://raw.github.com/afollestad/Cards-UI/master/images/device-2013-08-15-121417_framed.png)

Dependencies
=============
1. Silk (http://github.com/afollestad/Silk)

Getting Started
=============
Before the library will work, make sure you have `Silk` referenced as a dependency in both this library and your app.
You also need to copy the `assets` folder from `Silk` to your app due to the fact that this library uses font resources.

Implementing this library in your own apps is pretty simple. First, you need an XML layout that will contain the `ListView`
that displays your cards; it's recommended that you use the `CardListView` class instead of a stock `ListView`, as it
automates many things (such as notifying card headers that they were clicked and disabling the ListView divider and selector).

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.afollestad.cardsui.CardListView xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@+id/cardsList"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

Next, you get a reference to the `CardListView` from your code, create a `CardAdapter`, and attach it to the list:

```java
CardListView cardsList = (CardListView) findViewById(R.id.cardsList);
CardAdapter cardsAdapter = new CardAdapter(this);
cardsList.setAdapter(cardsAdapter);

// You can still use the regular OnItemClickListener for the list
cardsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // But this will never be called if you disable individual cards or the entire CardAdapter from being clickable
        // It will also not be called for card headers with actions set to them.
    }
});
```

Last, you just add cards and card headers to the adapter and the list will update itself automatically:

```java
cardsAdapter.add(new CardHeader("Week Days"));
cardsAdapter.add(new Card("Monday", "Back to work!"));
cardsAdapter.add(new Card("Tuesday", "Arguably the worst day of the week."));
cardsAdapter.add(new Card("Wednesday", "Hump day, almost done!"));
```

Card headers optionally take a second string for a subtitle.

**To use string resources for titles**, you just pass a context and integer in place of the strings.

Card Popup Menus
===============
This library allows you to set a menu that will be displayed in a popup when the little 3-dot button is tapped on cards,
just like Google Play. There's two ways to do this.

You can set a popup menu for every card at once through the adapter, this **must be called before you add any cards** to
the adapter:

```java
cardsAdapter.setPopupMenu(R.menu.card_popup, new Card.CardMenuListener() {
    @Override
    public void onMenuItemClick(Card card, MenuItem item) {
        Toast.makeText(getApplicationContext(), card.getTitle() + ": " + item.getTitle(), Toast.LENGTH_SHORT).show();
    }
});
```

You can also set a menu for individual cards, **you can have a menu set to the adapter and individual cards at the same time**,
but individual card menus will take priority:

```java
Card card = new Card("Tuesday", "Arguably the worst day of the week.")
        .setPopupMenu(R.menu.tuesday_popup, new Card.CardMenuListener() {
            @Override
            public void onMenuItemClick(Card card, MenuItem item) {
                Toast.makeText(getApplicationContext(), card.getTitle() + ": " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
cardsAdapter.add(card);
```

Card Header Actions
==============
If you ever use Google Play, you've seen how they display a "See More" button next to section headers. This library
makes it easy to do the same:

```java
CardHeader clickableHeader = new CardHeader("Week Days")
    .setAction("See More", new CardHeader.ActionListener() {
        @Override
        public void onClick(CardHeader header) {
            Toast.makeText(getApplicationContext(), header.getTitle(), Toast.LENGTH_LONG).show();
        }
    });
cardsAdapter.add(clickableHeader);
```

The `CardListView` handles calling the header's `ActionListener` for you, if you use a plain `ListView` you'll have to
manually do so through the ListView's OnItemClickListener.

**To use string resources for titles**, you just pass a context and integer in place of the string.

Card Thumbnails
===============
Adding a thumbnail to a card requires a single method:

```java
Card card = new Card("Hi", "Hello").setThumbnail();
```

There's 3 variations of `setThumbnail()`, allowing you to pass a `Drawable`, a `Bitmap`, or a drawable resource ID (which reqires you to pass a `Context` too).

Custom Card Layouts
==============
This library allows you to easily modify the look of cards, modifying headers is not supported, at least for now. First,
you will need a custom card layout. The quickest way to start with this would be to copy the `list_item_card.xml`
layout resource file from this project into your own and modify its contents. There's a specific set of views you need
to have in your layout:

1. An `ImageView` with the ID `@android:id/icon`, this is used to display card thumbnails.
2. A `TextView` with the ID `@android:id/title`, this is used to display card titles.
3. A `TextView` with the ID `@android:id/content`, this is used to display card content.
4. A `View` of any type with the ID `@android:id/button1`, this is used as the anchor for card popup menus.
5. And any other customizations that you want, even additional views (**see the bottom of this section for more details on this**).

To associate your layout with cards, you can either set the layout to the `CardAdapter` to apply the layout to every card:

```java
cardsAdapter.setCardLayout(R.layout.list_item_card_larger);
```

Or you can apply the layout to a single card before (or while) you add it to the adapter:

```java
cardsAdapter.add(new Card("Title", "Content")
                .setLayout(R.layout.list_item_card_larger));
```

If you add additional views to your card layout, you will have to override `CardAdapter` like this:

```java
public class CustomCardAdapter extends CardAdapter {

    public CustomCardAdapter(Context context) {
        super(context);
    }

    @Override
    public View onViewCreated(int index, View recycled, CardBase item) {
        // Get a reference to your custom view and do whatever you want with it
        View customView = recycled.findViewById(R.id.customView);
        // Let the CardAdapter handle the rest, it will recycle list views and everything for you
        return super.onViewCreated(index, recycled, item);
    }
}
```

Other Customizations
==============
This library's customization abilities are very limited at for now, but it will allow you to change the color used for header actions
and the title of cards:

```java
// You can pass resolved colors
cardsAdapter.setAccentColor(Color.parseColor("#0099CC"));
// Or color resources
cardsAdapter.setAccentColorRes(android.R.color.holo_blue_dark);
```

**This must be called before adding cards**, otherwise it will not work correctly.

Other Methods
===============
There's two other useful methods in the `Card` class and `CardsAdapter`:

`Card.setTag(Object)` -- this method allows you to associate an Object of any type with the card. This can be useful for keeping track of cards
by something other than their title.

`Card.setClickable(boolean)` -- by default a card is clickable; this allows you to disable a card from being highlighted when it's clicked (the white part).
Card popup menus will still function, however. You cannot disable headers, they will be unclickable by default unless you set
 an action to them.

`CardsAdapter.setCardsClickable(boolean)` -- the same function as calling `setClickable(boolean)` on a single card, but this applies
to every card in the adapter. This will override the individual isClickable value for a single card. This will not affect card popup menus or card header actions.