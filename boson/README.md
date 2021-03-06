# Boson Protocol version 1

Boson is a language independent binary protocol for object serialization and remote method invocation.
While the base types are based on Java's primitives, the bit size of data type is given
to make it uniform across languages.

Basic data types
--

The protocol handles 10 primitive types

See [Java datatypes](http://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html) for more

# Primitives

+ __byte__ => 8 bit signed two's compliment integer
+ __short__ => 16 bit signed two's complement integer
+ __int__ => 32 bit signed two's complement integer
+ __long__ => 64 bit signed two's compliment integer
+ __float__ => single-precision 32-bit IEEE 754 floating point
+ __double__ => double-precision 64-bit IEEE 754 floating point
+ __boolean__ => 1 byte 1 or 0 where 1 === true and 0 === false where 1 = 0x1 and 0 = 0x0
+ __char__ => 16-bit Unicode character. minimum value of '\u0000' (or 0) and a maximum value of '\uffff' (or 65,535 inclusive)
+ __null__ => Indicates a nullable value, if sent in place of a numeric field that field will be set to 0
+ __string__ => A sequence of characters, any valid UTF-8 string

# Structures

+ __array__ => An ordered set of items, the items can be any valid Boson data type
+ __list__ => An un-ordered group of items, the items can be any valid Boson data type
+ __set__ => An un-ordered group of __unique__ items, the items can be any valid Boson data type
+ __map__ => A set of key value pairs, both keys and values can be any valid Boson data type, including map itself
+ __enum__ => An enumeration of fixed/predefined values
+ __POLO__ => __P__lain __O__ld __L__anguage __O__bject - A POLO is any object whose fields are valid Boson types.

# Miscellaneous

+ __References__ => Furthermore, as circular references can become an issue in some languages (e.g. Java, C++),
                    reference types are supported. A reference is a Boson __int__ which refers to a boson __POLO__ type.

                    While possible it is not required for all structures to be references, only in places where it would
                    create a circular reference is it required. As such, de-serializers should use the Boson flag byte
                    to determine what type it is to read.

Encoding/Decoding
--

### Protocol version

In all cases the first thing to send is the protocol version the message is encoded with.

+ the protocol version is __1 byte__ so -128 to 127 e.g. __0x1__ = protocol version 1

### Size

Once the protocol version is written it must be immediately followed by the size of the message

+ The size is __4 bytes__ of the message, i.e. a __32 bit signed int__ from the second to the 5th byte
+ A side effect of this is that a message is limited to about 2GB
+ Size excludes the first 5 bytes (protocol version and message size) - i.e. the size is the total bytes of the payload

### Payload

The payload of the message immediately follows the message size i.e. the 6th byte onwards.

#### Indicating a type

The value should always preceded by the type.
To indicate a type use a single byte which corresponds to the following data types, for:

+ __byte__ => 1
+ __short__ => 2
+ __int__ => 3
+ __long__ => 4
+ __float__ => 5
+ __double__ => 6
+ __boolean__ => 7
+ __char__ => 8
+ __null__ => 9
+ __string__ => 10
+ __array__ => 11
+ __list__ => 12
+ __map__ => 13
+ __POLO__ => 14
+ __REFERENCE__ => 15
+ __set__ => 16
+ __enum__ => 17

### Indicating size

Where required the size should immediately follow the type.
The size is a 32 bit signed int. However, the total size of all types in the payload contribute to the total size
of the message.

+ __byte__ => N/A a byte is always 8 bits
+ __short__ => N/A a short is always 16 bits, i.e. 2 bytes
+ __int__ => N/A an int is always 32 bits, i.e. 4 bytes
+ __long__ => N/A a long is always 64 bits, i.e. 8 bytes
+ __float__ => N/A a float is always 32 bits, i.e. 4 bytes
+ __double__ => N/A a double is always 64 bits, i.e. 8 bytes
+ __boolean__ => N/A a boolean is always 1 byte, i.e 1 or 0 - This is always converted to true or false in languages
				that support this
+ __char__ => N/A a char is always 16 bits
+ __null__ => N/A once the type is given as null it is enough and the next byte should be the start of the next part of the payload
+ __string__ => 4 bytes (int) - this is the total number of bytes that make up the whole string i.e. size of all
				chars in the string __NOT__ the number of chars but the size of all the chars when converted to bytes
+ __array__ => 4 bytes  - This is __not the total bytes__ it is a __count/sum__ of how many items are in the array
+ __list__ => 4 bytes  - This is __not the total bytes__ it is a __count/sum__ of how many items are in the list
+ __set__ => 4 bytes  - This is __not the total bytes__ it is a __count/sum__ of how many items are in the set
+ __map__ => 4 bytes  - This is __not the total bytes__ it is a __count/sum__ of how many items are in the map
+ __POLO__ => 4 bytes - This is __not the total bytes of the object__, it is a __count/sum__ of how many fields from the
			object is serialized
+ __enum__ N/A see writing data structures below
+ __REFERENCE__ => 5 bytes, the first Byte is 15 (the boson type for a reference) and the following for bytes is an integer
                   representing the numeric reference.

### Writing data structures

####  array
 An array contains several items, each of which can be any supported Boson data type.

1. To write an array first write the type
2. followed by the total number of elements in the array.
3. Next, write each element according to the rules for each type, __in order__.

####  list
The rules for a list are the same as an array , __EXCEPT__ Do not write component types and the order of elements does not matter

#### set
The rules for a set is the same as a list, __EXCEPT__ all items are unique according to .equals() - i.e. calling .equals on any two items should return false

####  map
A map contains a __unordered__ set of tuples (key value pairs). Both keys and values can be any valid Boson data type.

1. To write a map, first write the type
2. followed by the total number of elements in the map.
3. Next, write the key according to the rules for its type
4. then write the value according to the rules for its type.

Both key and value can be empty. If either are empty then a the boson type null, should be written.

#### enum

An enum is made up of 3 components.
1. The Boson type for an enum as given above
2. The fully qualified name of the enum class, as a string
3. The string value of the enum constant that can be used to get the typed value back using .valueOf

These three are written in sequence. The rules for writing a string applies when writing the class name and value.

#### POLO
A POLO contains a __unordered__ set of fields. These fields have a name and a value.
Field names are strings and values can be any valid Boson data type.
In languages that are not type safe this is the same as a boson map. And the POLO flag __must not__ be used in those languages, instead the boson map data type should be used! The reason for this is that POLOs have a strict requirement for fully qualified class names to be provided.
Every POLO is given a unique reference number. This is an integer value that will be used to refer to it later if it is used in multiple places.

1. To write a POLO, first write the type
2. Then write the integer reference number for the POLO. (Just write the 4 bytes for the int, no need for the boson in type prefix)
3. Then write the fully qualified name of the POLO, e.g. com.domain.MyClass as a string using the rules for writing strings
4. followed by the total number of elements in the POLO.
5. Next, write each field name according to the rules for a __string__
6. Immediately after each field name write the value according to the rules for its type
__Values can be empty but not names__. If a field name is null, skip and do not serialize.


The following is a simple flow chart of the above process

![Boson POLO serialization](polo-serialization.png?raw=true)


RPC Serialization
---

To allow for other data types to be added and be continuous both connection and responses will use types starting from -127 in increasing order.

# Request

A connection has 3 components to it.

1. __method__ => A remote method name, this is a __string__ of arbitrary length/content used to identify which method is invoked on the remote service.
2. __callback__ => A __string__ which contains the name of the function to be invoked on the client when a response is received for a given connection.
3. __parameters__ => An __array__ of values, which can be any valid boson data type, these are the __ordered__ parameters that will be passed to the remote method.

The order the method name, callback and parameters are sent in __must be (method,callback,parameters)__.
This will allow partial de-serialization and make de-serializing POLOs easier in statically typed languages

### Types/Flags

+ __method name__   -127 (minus one hundred and twenty seven)
+ __parameters__   -126 (minus one hundred and twenty six)
+ __callback__   -125 (minus one hundred and twenty five)

### Serialization

1. Write the connection type (-127,-126 or -125)
2. Write the contents of the connection type using the rules for that content's type ( e.g if its an int write int flag then the int)

# Response

A connection has 2 components to it.

1. __method__ => A __string__ which contains the name of the function to be invoked on the client when the response is received.
3. __parameters__ => An __array__ of values, which can be any valid boson data type, these are the __ordered__ parameters that will be passed to the function on the client side.
Functions only return a single value but if the remote service wants to provide additional data such as an error message, or flag it can do so in this array.

If the length of the array in the second component is more than 1 then the __first__ value must always be the value of the response returned from the invoked function.

If the client callback only accept a single parameter (the response) then the client should, optionally log all the other fields received in the response array.


### Types/Flags

+ __method name__   -124 (minus one hundred and twenty four)
+ __parameters__   -123 (minus one hundred and twenty three)

### Serialization

1. Write the response type (-124,-123)
2. Write the contents of the response type using the rules for that content's type ( e.g if its an int write int flag then the int)

The order the method name and parameters are sent in __must be (method,parameters)__.
This will allow partial de-serialization and make de-serializing POLOs easier in statically typed languages

Streaming (Protocol version 2)
---------

An optional component of the protocol is to support streaming. Since Boson is a binary protocol as such, there are
going to be times when a 2GB size limit is not enough.

For now, this section will not be rigidly specified but as a vague idea:
Streaming can be supported by, in all cases where a size would have otherwise been specified, it is omitted.
So a value is only type followed by the type's payload. All data following the type is assumed to be part of the
type's content until an end of type token is received.

Once the end of type token is received, the type of the next value must follow, and so on...
