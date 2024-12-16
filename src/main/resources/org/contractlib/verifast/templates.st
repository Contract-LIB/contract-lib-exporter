adt_decl(dts) ::= <<
<dts>

<dts: {dt |
inductive <dt.first.first> =
<dt.second.constrs: { cstr |
    <cstr.first>(<cstr.second: { p |<p.second> <p.first>}; separator=", ">)}; separator="\n  | ">;

}>
>>


