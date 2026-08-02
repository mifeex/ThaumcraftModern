package com.thaumcraftmodern.client.screen;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ThaumonomiconMarkupTest {
    @Test
    void parsesClassicTagsCaseInsensitivelyInDocumentOrder() {
        List<ThaumonomiconMarkup.Node> nodes = ThaumonomiconMarkup.parse(
                "До<Br>После<LINE><img>"
                        + "thaumcraft:textures/misc/research1.png:0:8:255:208:.5"
                        + "</IMG>Конец"
        );

        assertEquals(6, nodes.size());
        assertEquals("До", assertInstanceOf(
                ThaumonomiconMarkup.Text.class,
                nodes.get(0)
        ).value());
        assertInstanceOf(ThaumonomiconMarkup.Break.class, nodes.get(1));
        assertEquals("После", assertInstanceOf(
                ThaumonomiconMarkup.Text.class,
                nodes.get(2)
        ).value());
        assertInstanceOf(ThaumonomiconMarkup.Divider.class, nodes.get(3));
        ThaumonomiconMarkup.ImageSpec image = assertInstanceOf(
                ThaumonomiconMarkup.Image.class,
                nodes.get(4)
        ).spec();
        assertEquals("thaumcraft:textures/misc/research1.png", image.texture().toString());
        assertEquals(0, image.sourceX());
        assertEquals(8, image.sourceY());
        assertEquals(255, image.sourceWidth());
        assertEquals(208, image.sourceHeight());
        assertEquals(0.5F, image.scale());
        assertEquals("Конец", assertInstanceOf(
                ThaumonomiconMarkup.Text.class,
                nodes.get(5)
        ).value());
    }

    @Test
    void supportsHtmlBreakAndHorizontalRuleAliases() {
        List<ThaumonomiconMarkup.Node> nodes =
                ThaumonomiconMarkup.parse("A<br/>B<br />C<hr>D");

        assertEquals(7, nodes.size());
        assertInstanceOf(ThaumonomiconMarkup.Break.class, nodes.get(1));
        assertInstanceOf(ThaumonomiconMarkup.Break.class, nodes.get(3));
        assertInstanceOf(ThaumonomiconMarkup.Divider.class, nodes.get(5));
    }

    @Test
    void malformedOrUnknownTagsRemainVisibleInsteadOfDeletingText() {
        String source = "A<unknown>B</unknown><IMG>broken:image</IMG>C";

        List<ThaumonomiconMarkup.Node> nodes = ThaumonomiconMarkup.parse(source);

        assertEquals(1, nodes.size());
        assertEquals(
                source,
                assertInstanceOf(ThaumonomiconMarkup.Text.class, nodes.get(0)).value()
        );
    }

    @Test
    void parsesItalicAndBoldTextCaseInsensitivelyAndAllowsNesting() {
        List<ThaumonomiconMarkup.Node> nodes = ThaumonomiconMarkup.parse(
                "Обычный <I>курсив <B>оба</B></I> <b>жирный</B>"
        );

        assertEquals(5, nodes.size());
        ThaumonomiconMarkup.Text italic = assertInstanceOf(
                ThaumonomiconMarkup.Text.class, nodes.get(1));
        assertEquals("курсив ", italic.value());
        assertEquals(true, italic.italic());
        assertEquals(false, italic.bold());

        ThaumonomiconMarkup.Text both = assertInstanceOf(
                ThaumonomiconMarkup.Text.class, nodes.get(2));
        assertEquals("оба", both.value());
        assertEquals(true, both.italic());
        assertEquals(true, both.bold());

        ThaumonomiconMarkup.Text bold = assertInstanceOf(
                ThaumonomiconMarkup.Text.class, nodes.get(4));
        assertEquals("жирный", bold.value());
        assertEquals(false, bold.italic());
        assertEquals(true, bold.bold());
    }
}
