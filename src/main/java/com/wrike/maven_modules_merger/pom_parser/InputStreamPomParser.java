package com.wrike.maven_modules_merger.pom_parser;

import com.wrike.maven_modules_merger.pom_parser.utils.XmlUtils;

import java.io.InputStream;

/**
 * Author: Daniil Shylko
 * Date: 31.08.2022
 */
public class InputStreamPomParser extends AbstractPomParser {

    public InputStreamPomParser(InputStream inputStream) {
        super(XmlUtils.readXml(inputStream));
    }

}
