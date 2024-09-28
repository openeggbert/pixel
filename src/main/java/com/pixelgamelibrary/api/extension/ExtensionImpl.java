///////////////////////////////////////////////////////////////////////////////////////////////
// Pixel: Game library.
// Copyright (C) 2024 the original author or authors.
//
// This program is free software: you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation, either version 3
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see 
// <https://www.gnu.org/licenses/> or write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
///////////////////////////////////////////////////////////////////////////////////////////////
package com.pixelgamelibrary.api.extension;

import com.pixelgamelibrary.api.PixelException;
import com.pixelgamelibrary.api.interfaces.Extension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author robertvokac
 */
public class ExtensionImpl implements Extension {

    private static final ExtensionImpl INSTANCE = new ExtensionImpl();

    public static Extension getInstance() {
        return INSTANCE;
    }

    public ExtensionImpl() {

    }
    // Map to hold registered implementations
    private final Map<Class<?>, Object> implementations = new HashMap<>();

// Implement the method for registering implementations    
    @Override
    public <T> void register(Class<T> interfaceClass, T implementation) {
        if(interfaceClass == Extension.class) {
            throw new PixelException("Custom implementation of interface Extension cannot be registred.");
        }
        implementations.put(interfaceClass, implementation);
        System.out.println("Registered implementation for: " + interfaceClass.getName());
    }

// Implementation of the generic method getImplementation    \
    @Override
    public <T> T get(Class<T> clazz) {
        T implementation = (T) implementations.get(clazz);
        if (implementation == null) {
            throw new IllegalArgumentException("No implementation registered for: " + clazz.getName());
        }
        return implementation;
    }

    @Override
    public <T> boolean has(Class<T> clazz) {
        return implementations.containsKey(clazz);
    }

    @Override
    public List<Class<?>> list() {
        return implementations.keySet().stream().collect(Collectors.toList());
    }

}
