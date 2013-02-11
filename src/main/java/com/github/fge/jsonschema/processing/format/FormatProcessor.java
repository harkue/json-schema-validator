/*
 * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.fge.jsonschema.processing.format;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.format.FormatAttribute;
import com.github.fge.jsonschema.keyword.validator.KeywordValidator;
import com.github.fge.jsonschema.library.Dictionary;
import com.github.fge.jsonschema.processing.ProcessingException;
import com.github.fge.jsonschema.processing.Processor;
import com.github.fge.jsonschema.processing.ValidationData;
import com.github.fge.jsonschema.processing.build.FullValidationContext;
import com.github.fge.jsonschema.report.ProcessingReport;
import com.github.fge.jsonschema.util.NodeType;
import com.google.common.collect.Lists;

import java.util.List;

import static com.github.fge.jsonschema.messages.FormatMessages.*;

public final class FormatProcessor
    implements Processor<FullValidationContext, FullValidationContext>
{
    private final Dictionary<FormatAttribute> dict;

    public FormatProcessor(final Dictionary<FormatAttribute> dict)
    {
        this.dict = dict;
    }

    @Override
    public FullValidationContext process(final ProcessingReport report,
        final FullValidationContext input)
        throws ProcessingException
    {
        final ValidationData data = input.getValidationData();
        final JsonNode node = data.getSchema().getNode().get("format");

        if (node == null)
            return input;

        final String fmt = node.textValue();
        final FormatAttribute attr = dict.get(fmt);

        if (attr == null) {
            report.warn(input.newMessage().put("domain", "validation")
                .put("keyword", "format").msg(FORMAT_NOT_SUPPORTED)
                .put("attribute", fmt));
            return input;
        }

        final NodeType type
            = NodeType.getNodeType(data.getInstance().getNode());

        if (!attr.supportedTypes().contains(type))
            return input;

        final List<KeywordValidator> validators = Lists.newArrayList(input);
        validators.add(formatValidator(attr));

        return new FullValidationContext(data, validators);
    }

    private static KeywordValidator formatValidator(final FormatAttribute attr)
    {
        return new KeywordValidator()
        {
            @Override
            public void validate(
                final Processor<ValidationData, ProcessingReport> processor,
                final ProcessingReport report, final ValidationData data)
                throws ProcessingException
            {
                attr.validate(report, data);
            }
        };
    }
}