package de.jobst.resulter.adapter.driven.jdbc.transformer;

import com.turkraft.springfilter.parser.node.*;
import com.turkraft.springfilter.transformer.FilterNodeTransformer;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.ExampleMatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("ClassCanBeRecord")
public class MappingFilterNodeTransformer implements FilterNodeTransformer<MappingFilterNodeTransformResult> {

    protected final ConversionService conversionService;

    public MappingFilterNodeTransformer(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    
    @Override
    public Class<MappingFilterNodeTransformResult> getTargetType() {
        return MappingFilterNodeTransformResult.class;
    }

    
    private static final java.util.regex.Pattern VALID_FIELD_NAME =
            java.util.regex.Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*(?:\\.[a-zA-Z][a-zA-Z0-9_]*)*");

    @Override
    public MappingFilterNodeTransformResult transformField(FieldNode node) {
        String name = node.getName();
        if (!VALID_FIELD_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid filter field name: " + name);
        }
        return new MappingFilterNodeTransformResult(name);
    }

    
    @Override
    public MappingFilterNodeTransformResult transformInput(InputNode node) {
        return new MappingFilterNodeTransformResult("'" +
                                                    Objects.requireNonNull(conversionService.convert(node.getValue(),
                                                                String.class),
                                                            "Could not convert `" + node.getValue() + "` to string")
                                                        .replace("'", "\\'") + "'");
    }

    
    @Override
    public MappingFilterNodeTransformResult transformPriority(PriorityNode node) {
        MappingFilterNodeTransformResult nodeTransform = transform(node.getNode());
        Map<String, ValueWithMatcher> filterMap = new HashMap<>(nodeTransform.filterMap());
        return new MappingFilterNodeTransformResult("(" + nodeTransform + ")", filterMap);
    }

    
    @Override
    public MappingFilterNodeTransformResult transformPlaceholder(PlaceholderNode node) {
        return new MappingFilterNodeTransformResult("`" + node.getPlaceholder().getName() + "`");
    }

    
    @Override
    public MappingFilterNodeTransformResult transformFunction(FunctionNode node) {
        return new MappingFilterNodeTransformResult(node.getFunction().getName() + "(" + node.getArguments()
            .stream()
            .map(this::transform)
            .map(MappingFilterNodeTransformResult::filterString)
            .collect(Collectors.joining(", ")) + ")");
    }

    
    @Override
    public MappingFilterNodeTransformResult transformCollection(CollectionNode node) {
        return new MappingFilterNodeTransformResult("[" + node.getItems()
            .stream()
            .map(this::transform)
            .map(MappingFilterNodeTransformResult::filterString)
            .collect(Collectors.joining(", ")) + "]");
    }

    
    @Override
    public MappingFilterNodeTransformResult transformPrefixOperation(PrefixOperationNode node) {
        MappingFilterNodeTransformResult nodeTransform = transform(node.getRight());
        Map<String, ValueWithMatcher> filterMap = new HashMap<>(nodeTransform.filterMap());
        return new MappingFilterNodeTransformResult(node.getOperator().getToken() + " " + nodeTransform, filterMap);
    }

    
    @Override
    public MappingFilterNodeTransformResult transformInfixOperation(InfixOperationNode node) {
        Map<String, ValueWithMatcher> filterMap = new HashMap<>();
        MappingFilterNodeTransformResult leftTransform = transform(node.getLeft());
        MappingFilterNodeTransformResult rightTransform = transform(node.getRight());
        filterMap.putAll(leftTransform.filterMap());
        filterMap.putAll(rightTransform.filterMap());
        if (node.getOperator().getToken().equals("~~")) {
            filterMap.put(leftTransform.filterString(),
                new ValueWithMatcher(rightTransform.filterString(), ExampleMatcher.StringMatcher.CONTAINING));
        }
        return new MappingFilterNodeTransformResult(
            leftTransform + " " + node.getOperator().getToken() + " " + rightTransform, filterMap);
    }

    
    @Override
    public MappingFilterNodeTransformResult transformPostfixOperation(PostfixOperationNode node) {
        return new MappingFilterNodeTransformResult(transform(node.getLeft()) + " " + node.getOperator().getToken());
    }

}
