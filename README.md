# caro

Given JSON structures on stdin:

- walk the structure to the given paths
- shell out to to a different program and pass it the value at that point in the tree
- replace the value in the tree with stdout of the program, parsed as JSON (or as a string if it's not)
- produce the result on stdout

Named because it's Tree **E***val **A***nd **R**eplace and "caro caro" is also known as
the [elephant-ear tree][eet] because it produces ear-shaped seed pods.

[eet]: https://en.wikipedia.org/wiki/Enterolobium_cyclocarpum

## Usage

The following samples are all evaluated in a POSIX sh. A line that starts with a
comment is what will show up on stdout from the previous command.

Here we'll define a script that takes a number and increments it just so we can
demo what is going on in future examples:

```
cat <<EOF > inc
#!/usr/bin/env python3
import sys
import json
print(int(json.load(sys.stdin)) + 1)
EOF
chmod +x inc
```

Note that the output of this script will conveniently also be valid JSON because
it's just the decimal representation of an integer.

```
echo "5" | ./inc
# 6
```

Let's increment a value in a tree:

```
echo '{"a": 1}' | caro ./inc '["a"]'
# {"a": 2}
```

Paths can traverse multiple levels:

```
echo '{"a": {"b": {"c": 1}}}' | caro inc '["a" "b" "c"]'
# {"a": {"b": {"c": 2}}}
```

Pass multiple paths to replace multiple parts in the tree:

```
echo '{"a": 1, "b": 1}' | caro ./inc '["a"]' '["b"]'
# {"a": 2, "b": 2}
```

The command output itself is parsed as JSON, and failing that interpreted as a
string. Consecutive JSON inputs may be given, which produce consecutive JSON
outputs. Of course, this requires the command passed to caro will also get
multiple JSON objects on stdin and produce JSON outputs on stdout. Our demo
which our `inc` doesn't, so we create `inc2`:

```python3
#!/usr/bin/env python3
import sys
import json

def json_multi_load(s):
    dec = json.JSONDecoder()
    vals, idx = [], 0
    try:
        while True:
            val, idx = dec.raw_decode(s, idx)
            vals.append(val)
    except json.JSONDecodeError:
        return vals

for v in json.multi_load(sys.stdin.read()):
    print(json.dump({"iddqd": v["xyzzy"] + 1}))
```

This yields the following result:

```
echo '{"a": {"xyzzy": 1}}{"a": {"xyzzy": 2}}' | caro ./inc2 '["a"]'
# {"a":{"iddqd":2}}{"a":{"iddqd":3}}%
```

Note that the subcommand saw:

```
{"xyzzy": 1}{"xyzzy": 2}
```

The structure around it (here, the `"a"` key) was parsed and reconstituted by
caro.

If you wanted to just call each command multiple times instead of taking
multiple JSONs on standard input, call caro multiple times. caro works this way
so you can use tools that compare "adjacent" JSON data. Unsurprisingly, caro
pairs quite well with [reciffist][recidiffist] and
[recidiffist-cli][recidiffist-cli] in particular.

[recidiffist]: https://github.com/latacora/recidiffist
[recidiffist-cli]: https://github.com/latacora/recidiffist-cli

## License

Copyright © Latacora

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
